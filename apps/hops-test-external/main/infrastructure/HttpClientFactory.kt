package infrastructure

import com.nimbusds.jose.jwk.RSAKey
import infrastructure.maskinporten.client.MaskinportenClient
import infrastructure.maskinporten.client.MaskinportenConfig
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.AuthProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader

object HttpClientFactory {
    fun create(config: Configuration.ExternalApi) =
        HttpClient {
            install(Auth) {
                val maskinportenConfig = MaskinportenConfig(
                    "https://${config.discoveryUrl.host}",
                    config.clientId,
                    RSAKey.parse(config.clientJwk),
                    config.scope,
                    resource = config.audience,
                )

                val maskinportenClient = MaskinportenClient(maskinportenConfig)
                providers.add(MaskinportenOAuth2Provider(maskinportenClient))
            }
        }
}

private class MaskinportenOAuth2Provider(
    private val client: MaskinportenClient
) : AuthProvider {
    override val sendWithoutRequest = true
    override fun isApplicable(auth: HttpAuthHeader) = true

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers[HttpHeaders.Authorization] = "Bearer ${client.maskinportenTokenString}"
    }
}
