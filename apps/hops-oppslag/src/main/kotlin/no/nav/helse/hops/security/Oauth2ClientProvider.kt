package no.nav.helse.hops.security

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.auth.*

/**
 * Add [Oauth2ClientProvider] to client [Auth] providers.
 */
fun Auth.oauth2(configClient: Oauth2ClientProviderConfig) = providers.add(Oauth2ClientProvider(configClient))

data class Oauth2ClientProviderConfig(
    val tokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val scope: String = "",
    val sendWithoutRequest: Boolean = false
)

/**
 * Oauth2 client-credentials-flow authentication provider.
 */
private class Oauth2ClientProvider(
    private val configClient: Oauth2ClientProviderConfig
) : AuthProvider {
    override val sendWithoutRequest: Boolean = configClient.sendWithoutRequest
    override fun isApplicable(auth: HttpAuthHeader): Boolean = true

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers[HttpHeaders.Authorization] = constructOauth2TokenValue()
    }

    private suspend fun constructOauth2TokenValue(): String {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("scope", configClient.scope)
            append("client_id", configClient.clientId)
            append("client_secret",configClient.clientSecret)
        }

        /*
        Should probably keep around a single httpclient instance instead of re-creating it.
        Should also probably cache the token and periodically refresh it.
         */
        HttpClient {
            install(JsonFeature)
        }.use { client ->
            val response = client.submitForm<Oauth2Response>(
                configClient.tokenUrl,
                parameters)

            return "Bearer ${response.access_token}"
        }
    }

    private data class Oauth2Response(
        val access_token: String,
        val scope: String,
        val token_type: String,
        val expires_in: Int
    )
}