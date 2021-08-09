package no.nav.helse.hops.security

import io.ktor.client.features.auth.AuthProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import no.nav.helse.hops.hops.security.oauth.IOAuth2Client

class OAuth2Provider(
    private val client: IOAuth2Client,
    private val scope: String
) : AuthProvider {
    override val sendWithoutRequest = true
    override fun isApplicable(auth: HttpAuthHeader) = true

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        client.getToken(scope).also {
            request.headers[HttpHeaders.Authorization] = "Bearer $it"
        }
    }
}
