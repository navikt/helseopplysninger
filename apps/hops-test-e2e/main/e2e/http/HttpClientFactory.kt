package e2e.http

import com.nimbusds.jose.jwk.RSAKey
import e2e.api.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.AuthProvider
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.auth.HttpAuthHeader
import kotlinx.serialization.json.Json
import no.nav.helse.hops.maskinporten.MaskinportClient
import no.nav.helse.hops.maskinporten.MaskinportConfig

internal object HttpClientFactory {
    fun create(config: ApiConfig.Maskinporten, vararg feature: HttpFeature): HttpClient =
        HttpClient {
            if (feature.contains(HttpFeature.MASKINPORTEN)) {
                install(Auth) { providers.add(MaskinportAuthenticator(config)) }
            }
            if (feature.contains(HttpFeature.KOTLINX)) {
                install(JsonFeature) { serializer = KotlinxSerializer(Json) }
            }
        }
}

internal enum class HttpFeature { MASKINPORTEN, KOTLINX }

private class MaskinportAuthenticator(config: ApiConfig.Maskinporten) : AuthProvider {
    override val sendWithoutRequest = true
    override fun isApplicable(auth: HttpAuthHeader) = true
    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.header(HttpHeaders.Authorization, "Bearer ${maskinporten.jwt.parsedString}")
    }

    private val maskinporten = MaskinportClient(
        MaskinportConfig(
            baseUrl = config.discoveryUrl.withoutPath,
            clientId = config.clientId,
            privateKey = RSAKey.parse(config.clientJwk),
            scope = config.scope,
            resource = config.audience,
            issuer = config.issuer,
        )
    )
}

private val String.withoutPath: String get() = removeSuffix(Url(this).encodedPath)
