package no.nav.helse.hops.oppslag.security

import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.api.IHttpRequest
import ca.uhn.fhir.rest.client.api.IHttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.security.oauth.IOAuth2Client

class OauthRequestInterceptor(
    private val oauth2Client: IOAuth2Client,
    private val scope: String
) : IClientInterceptor {
    override fun interceptRequest(theRequest: IHttpRequest?) {

        val token = runBlocking {
            oauth2Client.getToken(scope)
        }
        theRequest?.addHeader(HttpHeaders.Authorization, "Bearer $token")
    }

    override fun interceptResponse(theResponse: IHttpResponse?) {}
}