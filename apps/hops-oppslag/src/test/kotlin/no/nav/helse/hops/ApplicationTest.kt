package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `existing behandler with valid JWT should give 200`() {
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Get, "/behandler/9111492") {
                    addHeader("Authorization", "Bearer ${oauthServer.issueToken().serialize()}")
                }
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Våge", response.content)
            }
        }
    }

    @Test
    fun `behandler with missing JWT should give 401-Unauthorized`() {
        withHopsTestApplication {
            with(handleRequest(HttpMethod.Get, "/behandler/9111492")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `behandler with invalid JWT should give 401-Unauthorized`() {
        withTestApplication({
            doConfig(
                acceptedAudience = "some-audience",
                acceptedIssuer = "some-issuer"
            )
            module()
        }) {
            with(
                handleRequest(HttpMethod.Get, "/behandler/9111492") {
                    addHeader("Authorization", "Bearer ${oauthServer.issueToken(audience = "not-accepted").serialize()}")
                }
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            with(
                handleRequest(HttpMethod.Get, "/behandler/9111492") {
                    addHeader("Authorization", "Bearer ${oauthServer.issueToken(issuerId = "not-accepted").serialize()}")
                }
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/isReady", "isAlive"])
    fun `endpoint with missing JWT should give_200`(uri: String) {
        withHopsTestApplication {
            with(handleRequest(HttpMethod.Get, uri)) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            doConfig()
            module()
        }) {
            test()
        }
    }

    private fun Application.doConfig(
        acceptedIssuer: String = "default",
        acceptedAudience: String = "default"
    ) {
        (environment.config as MapApplicationConfig).apply {
            put("no.nav.helse.hops.fkr.baseUrl", fkrServer.url("/").toString())
            put("no.nav.helse.hops.fkr.tokenUrl", "${oauthServer.tokenEndpointUrl(acceptedIssuer)}")
            put("no.nav.helse.hops.fkr.clientId", "test-client-id")
            put("no.nav.helse.hops.fkr.clientSecret", "test-secret")
            put("no.nav.helse.hops.fkr.scope", "test-scope")
            put("no.nav.security.jwt.issuers.size", "1")
            put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
            put("no.nav.security.jwt.issuers.0.discoveryurl", "${oauthServer.wellKnownUrl(acceptedIssuer)}")
            put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
        }
    }

    private companion object {
        private const val idTokenCookieName = "selvbetjening-idtoken"
        val oauthServer = MockOAuth2Server()
        val fkrServer = MockWebServer().apply { dispatcher = FkrMockDispatcher() }

        @BeforeAll
        @JvmStatic
        fun before() {
            oauthServer.start()
            fkrServer.start()
        }

        @AfterAll
        @JvmStatic
        fun after() {
            oauthServer.shutdown()
            fkrServer.shutdown()
        }
    }
}
