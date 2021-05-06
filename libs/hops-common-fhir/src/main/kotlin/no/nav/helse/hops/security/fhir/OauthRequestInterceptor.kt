package no.nav.helse.hops.security.fhir

import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.api.IHttpRequest
import ca.uhn.fhir.rest.client.api.IHttpResponse
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.security.oauth.IOAuth2Client

class OauthRequestInterceptor(
    private val oauth2Client: IOAuth2Client,
    private val scope: String
) : IClientInterceptor {
    override fun interceptRequest(theRequest: IHttpRequest?) {
        // OAuth2Client should handle caching and refreshing of token.
        val token = runBlocking {
            oauth2Client.getToken(scope)
        }
        theRequest?.addHeader("Authorization", "Bearer $token")
    }

    override fun interceptResponse(theResponse: IHttpResponse?) {}
}
