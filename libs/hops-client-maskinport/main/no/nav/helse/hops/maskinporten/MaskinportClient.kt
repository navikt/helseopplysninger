package no.nav.helse.hops.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.util.Date

class MaskinportClient(private val config: MaskinportConfig) {
    private val jwtCache: TokenCache = TokenCache()
    private val jwtGrant = JwtGrantFactory(config)
    private val client: HttpClient = HttpClient() { install(JsonFeature) } // response Content-Type: application/json

    val jwt: SignedJWT get() = jwtCache.getToken() ?: runBlocking { jwtCache.update(fetchToken()) }
    private suspend fun fetchToken(): Token = client.post("${config.baseUrl}/token") {
        contentType(ContentType.Application.FormUrlEncoded)
        body = "grant_type=$GRANT_TYPE&assertion=${jwtGrant.jwt}"
    }
}

data class MaskinportConfig(
    internal val baseUrl: String,
    internal val clientId: String,
    internal val privateKey: RSAKey,
    internal val scope: String,
    internal val validInSeconds: Int = 120,
    internal val resource: String,
    internal val issuer: String = baseUrl.suffix("/")
)

private class TokenCache(private var token: String? = null) {
    fun getToken(): SignedJWT? = token
        ?.let(SignedJWT::parse)
        ?.takeUnless { it.hasExpired }

    fun update(tokenResponse: Token): SignedJWT {
        token = tokenResponse.access_token
        return getToken() ?: error("new token has expired")
    }

    private val SignedJWT.hasExpired: Boolean get() = jwtClaimsSet?.expirationTime?.willExpireIn20Sec ?: false
    private val Date.willExpireIn20Sec: Boolean get() = time < (Date() plusSeconds 20).time
}

class JwtGrantFactory(private val config: MaskinportConfig) {
    val jwt: String get() = signedJwt.serialize()
    private val signedJwt get() = SignedJWT(jwsHeader, jwtClaimSet).apply { sign(RSASSASigner(config.privateKey)) }
    private val jwsHeader get() = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(config.privateKey.keyID).build()
    private val jwtClaimSet: JWTClaimsSet
        get() = JWTClaimsSet.Builder().apply {
            audience(config.issuer)
            issuer(config.clientId)
            issueTime(Date())
            expirationTime(Date() plusSeconds config.validInSeconds)
            claim("scope", config.scope)
            claim("resource", config.resource)
        }.build()
}

@Serializable
private data class Token(val access_token: String, val expires_in: Int, val scope: String)

const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
private infix fun Date.plusSeconds(seconds: Int): Date = Date(time + seconds * 1_000L)
private fun String.suffix(s: String) = if (endsWith(s)) this else plus(s)
