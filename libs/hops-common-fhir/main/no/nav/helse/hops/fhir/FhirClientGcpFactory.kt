package no.nav.helse.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.api.Constants
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.IHttpRequest
import ca.uhn.fhir.rest.client.api.IHttpResponse
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.runBlocking
import java.net.URL

object FhirClientGcpFactory {
    data class Config(
        val baseUrl: URL,
    )

    /** Creates a FHIR http-client. **/
    private fun create(baseUrl: URL): IGenericClient {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        val factory = ctx.restfulClientFactory.apply {
            // So that we dont start by requesting '/metadata'.
            serverValidationMode = ServerValidationModeEnum.NEVER
        }

        return factory.newGenericClient(baseUrl.toString())
    }

    /** Creates a FHIR http-client configured with an OAuth2 interceptor. **/
    fun createWithAuth(config: Config): IGenericClient {
        val credentials = GoogleCredentials
            .getApplicationDefault()
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

        return create(config.baseUrl).apply { registerInterceptor(GoogleInterceptor(credentials)) }
    }
    class GoogleInterceptor(
        private val credentials: GoogleCredentials,
    ) : IClientInterceptor {
        override fun interceptRequest(theRequest: IHttpRequest) {
            // OAuth2Client should handle caching and refreshing of token.
            runBlocking {
                credentials.refreshIfExpired()
            }
            val token = credentials.accessToken.tokenValue
            theRequest.addHeader(
                Constants.HEADER_AUTHORIZATION, (Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER + token)
            )
        }

        override fun interceptResponse(theResponse: IHttpResponse?) {}
    }
}
