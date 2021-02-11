package no.nav.helse.hops.security

import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.api.IHttpRequest
import ca.uhn.fhir.rest.client.api.IHttpResponse
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

data class Oauth2ClientProviderConfig(
    val tokenUrl: String,
    val clientId: String,
    val clientSecret: String,
    val scope: String
)

class OauthRequestInterceptor(private val _config: Oauth2ClientProviderConfig): IClientInterceptor {
    override fun interceptRequest(theRequest: IHttpRequest?) {

        // TODO: Should cache the token
        val response = runBlocking { constructOauth2TokenValue() }
        theRequest?.addHeader(HttpHeaders.Authorization, "Bearer ${response.access_token}")
    }

    override fun interceptResponse(theResponse: IHttpResponse?) {}

    private suspend fun constructOauth2TokenValue(): Oauth2Response {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("scope", _config.scope)
            append("client_id", _config.clientId)
            append("client_secret", _config.clientSecret)
        }

        HttpClient { install(JsonFeature) }.use { client -> return client.submitForm(_config.tokenUrl, parameters) }
    }

    private data class Oauth2Response(
        val access_token: String,
        val scope: String,
        val token_type: String,
        val expires_in: Int
    )
}