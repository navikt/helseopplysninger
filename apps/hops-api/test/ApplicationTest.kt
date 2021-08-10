import infrastructure.EVENT_STORE_CLIENT_NAME
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
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
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
        val testModule = module { single(named(EVENT_STORE_CLIENT_NAME)) { createEventStoreMockClient() } }
        withHopsTestApplication(testModule) {
            with(
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    val token = oauthServer.issueToken(claims = mapOf("scope" to "/test-subscribe"))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }

    private fun <R> withHopsTestApplication(testKoinModule: Module = Module(), test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            doConfig()
            module(testKoinModule)
        }) {
            test()
        }
    }

    private fun Application.doConfig(
        acceptedIssuer: String = "default",
        acceptedAudience: String = "default"
    ) {
        (environment.config as MapApplicationConfig).apply {
            put("no.nav.security.jwt.issuers.size", "1")
            put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
            put("no.nav.security.jwt.issuers.0.discoveryurl", "${oauthServer.wellKnownUrl(acceptedIssuer)}")
            put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
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
