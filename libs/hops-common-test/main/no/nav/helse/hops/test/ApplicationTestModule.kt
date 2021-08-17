package no.nav.helse.hops.test

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import no.nav.security.mock.oauth2.MockOAuth2Server

val oAuthMock = MockOAuth2Server()
fun startOAuthMock() = with(oAuthMock, MockOAuth2Server::start)
fun stopOAuthMock() = with(oAuthMock, MockOAuth2Server::shutdown)

fun Application.testConfig(
    oAuth2Server: MockOAuth2Server = oAuthMock,
    acceptedIssuer: String = "default",
    acceptedAudience: String = "default"
) {
    (environment.config as MapApplicationConfig).apply {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
        put("no.nav.security.jwt.issuers.0.discoveryurl", "${oAuth2Server.wellKnownUrl(acceptedIssuer)}")
        put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
        put("security.scopes.publish", "/test-publish")
        put("security.scopes.subscribe", "/test-subscribe")
    }
}
