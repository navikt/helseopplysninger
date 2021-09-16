package no.nav.helse.hops.maskinporten

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.util.Date

class MaskinportClient(config: MaskinportConfig) {
    private val tokenCache: TokenCache = TokenCache()
    private val grantTokenGenerator = GrantTokenGenerator(config)
    private val httpClient: HttpClient = HttpClient.newBuilder().proxy(config.proxy).build()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    val token: SignedJWT get() = tokenCache.getToken() ?: tokenCache.update(fetchToken())

    private fun fetchToken(): String = httpClient.send(tokenRequest, ofString()).let { response ->
        when (response.statusCode()) {
            200 -> response.getTokenBody().access_token
            else -> error("Failed to get token: Status: ${response.statusCode()} Body: ${response.body()}")
        }
    }

    private val tokenRequest: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create(config.baseUrl + "/token"))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(request.asBodyPublisher)
        .build()

    private val request: String get() = "grant_type=$GRANT_TYPE&assertion=${grantTokenGenerator.jwt}"
    private val String.asBodyPublisher get() = HttpRequest.BodyPublishers.ofString(this)
    private fun HttpResponse<String>.getTokenBody(): MaskinportTokenBody = objectMapper.readValue(this.body())
}

internal class TokenCache(var tokenString: String? = null) {
    fun getToken(): SignedJWT? = tokenString
        ?.let(SignedJWT::parse)
        ?.takeUnless { it.hasExpired }

    fun update(newTokenString: String): SignedJWT {
        tokenString = newTokenString
        return getToken() ?: error("new token has expired")
    }

    private val SignedJWT.hasExpired: Boolean get() = jwtClaimsSet?.expirationTime?.willExpireIn20Sec ?: false
    private val Date.willExpireIn20Sec: Boolean get() = time < (Date() plusSeconds 20).time
}

internal class GrantTokenGenerator(private val config: MaskinportConfig) {
    internal val jwt: String
        get() = SignedJWT(signatureHeader, jwtClaimSet)
            .apply { sign(RSASSASigner(config.privateKey)) }
            .serialize()

    private val signatureHeader: JWSHeader
        get() = JWSHeader
            .Builder(JWSAlgorithm.RS256)
            .keyID(config.privateKey.keyID)
            .build()

    private val jwtClaimSet: JWTClaimsSet
        get() = JWTClaimsSet.Builder().apply {
            audience(config.issuer)
            issuer(config.clientId)
            claim("scope", config.scope)
            issueTime(Date())
            expirationTime(Date() plusSeconds config.validInSeconds)
            config.resource?.let { claim("resource", it) }
        }.build()
}

internal data class MaskinportTokenBody(
    val access_token: String,
    val token_type: String?,
    val expires_in: Int?,
    val scope: String?
)

data class MaskinportConfig(
    internal val baseUrl: String,
    internal val clientId: String,
    internal val privateKey: RSAKey,
    internal val scope: String,
    internal val validInSeconds: Int = 120,
    internal val proxy: ProxySelector = ProxySelector.getDefault(),
    internal val resource: String? = null,
    internal val issuer: String = baseUrl.suffix("/")
)

internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
internal infix fun Date.plusSeconds(seconds: Int): Date = Date(time + seconds * 1_000L)
internal fun String.suffix(s: String) = if (endsWith(s)) this else plus(s)
