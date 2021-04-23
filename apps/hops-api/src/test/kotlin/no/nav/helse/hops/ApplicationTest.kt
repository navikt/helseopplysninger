package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `Requests with without token should return 401-Unauthorized`() {
        withHopsTestApplication {
            with(handleRequest(Get, "/")) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `Tokens without correct scope hould be rejected and endpoint should return 401-Unauthorized`() {
        withHopsTestApplication {
            with(
                handleRequest(Get, "/") {
                    val token = oauthServer.issueToken(claims = mapOf("scope" to "nav:helse/v1/testScope"))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(Unauthorized, response.status())
            }

            with(
                handleRequest(Get, "/") {
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
            with(
                handleRequest(Get, "/") {
                    val token = oauthServer.issueToken(claims = mapOf("scope" to "nav:helse/v1/helseopplysninger"))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }

    fun `Requests for tasks without token should return 401-Unauthorized`() {
        withHopsTestApplication {
            with(handleRequest(Get, "/tasks")) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    fun `Requests for tasks with valid token and correct scope should should return 200-Ok`() {
        withHopsTestApplicationHapi {
            with(
                handleRequest(Get, "/tasks") {
                    val token = oauthServer.issueToken(claims = mapOf("scope" to "nav:helse/v1/helseopplysninger"))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }

    private fun <R> withHopsTestApplicationHapi(test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            doConfigHapi()
            api()
        }) {
            test()
        }
    }

    private fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            doConfig()
            api()
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
        }
    }

    private fun Application.doConfigHapi(
        acceptedIssuer: String = "default",
    ) {
        (environment.config as MapApplicationConfig).apply {
            put("no.nav.helse.hops.hapi.baseUrl", hapiServer.url("/").toString())
            put("no.nav.helse.hops.hapi.tokenUrl", "${oauthServer.tokenEndpointUrl(acceptedIssuer)}")
            put("no.nav.helse.hops.hapi.clientId", "test-client-id")
            put("no.nav.helse.hops.hapi.clientSecret", "test-secret")
            put("no.nav.helse.hops.hapi.scope", "test-scope")
        }
    }

    private companion object {
        val oauthServer = MockOAuth2Server()
        val hapiServer = MockWebServer().apply { dispatcher = HapiMockDispatcher() }

        @BeforeAll
        @JvmStatic
        fun before() {
            oauthServer.start()
            hapiServer.start()
        }

        @AfterAll
        @JvmStatic
        fun after() {
            oauthServer.shutdown()
            hapiServer.shutdown()
        }
    }
}
