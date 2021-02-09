package no.nav.helse.hops.common

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.auth.*

/**
 * Add [Oauth2Provider] to client [Auth] providers.
 */
fun Auth.oauth2(config: Oauth2ProviderConfig) = providers.add(Oauth2Provider(config))

data class Oauth2ProviderConfig(
    val tokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val scope: String = "",
    val sendWithoutRequest: Boolean = false
)

/**
 * Client basic authentication provider.
 */
private class Oauth2Provider(
    private val config: Oauth2ProviderConfig
) : AuthProvider {
    override val sendWithoutRequest: Boolean = config.sendWithoutRequest
    override fun isApplicable(auth: HttpAuthHeader): Boolean = true

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers[HttpHeaders.Authorization] = constructOauth2TokenValue()
    }

    private suspend fun constructOauth2TokenValue(): String {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("scope", config.scope)
            append("client_id", config.clientId)
            append("client_secret",config.clientSecret)
        }

        /*
        Should probably keep around a single httpclient instance instead of re-creating it.
        Should also probably cache the token and periodically refresh it.
         */
        HttpClient {
            install(JsonFeature)
        }.use { client ->
            val response = client.submitForm<Oauth2Response>(
                config.tokenUrl,
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