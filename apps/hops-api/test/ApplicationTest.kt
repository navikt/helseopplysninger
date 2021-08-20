import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `Requests without token should return 401-Unauthorized`() {
        withHopsTestApplication {
            with(handleRequest(Get, "/fhir/4.0/Bundle")) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `Tokens without correct scope hould be rejected and endpoint should return 401-Unauthorized`() {
        withHopsTestApplication {
            with(
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    val token = oauthServer.issueToken(claims = mapOf("scope" to "/test-wrong"))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(Unauthorized, response.status())
            }

            with(
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    val token = oauthServer.issueToken()
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `Requests with valid token and correct scope should should return 200-Ok`() {
        withHopsTestApplication {
            val token = oauthServer.issueToken(claims = mapOf("scope" to "/test-subscribe"))
            with(
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }

    private fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            oAuthConfig()
            this.
            mainWith(eventStoreMock())
        }) {
            test()
        }
    }

    private fun Application.oAuthConfig() {
        (environment.config as MapApplicationConfig).apply {
            put("no.nav.security.jwt.issuers.size", "1")
            put("no.nav.security.jwt.issuers.0.issuer_name", "test")
            put("no.nav.security.jwt.issuers.0.discoveryurl", "${oauthServer.wellKnownUrl("test")}")
            put("no.nav.security.jwt.issuers.0.accepted_audience", "test")
            put("security.scopes.publish", "/test-publish")
            put("security.scopes.subscribe", "/test-subscribe")
        }
    }

    private companion object {
        val oauthServer = MockOAuth2Server()

        @BeforeAll
        @JvmStatic
        fun before() {
            oauthServer.start()
        }

        @AfterAll
        @JvmStatic
        fun after() {
            oauthServer.shutdown()
        }
    }
}
