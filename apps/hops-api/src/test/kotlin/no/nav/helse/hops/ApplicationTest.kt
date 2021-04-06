package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.*
import no.nav.security.mock.oauth2.MockOAuth2Server
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
            with(handleRequest(Get, "/") {
                val token = oauthServer.issueToken(claims = mapOf("scope" to "nav:helse/v1/testScope"))
                addHeader("Authorization", "Bearer ${token.serialize()}")
            }) {
                assertEquals(Unauthorized, response.status())
            }

            with(handleRequest(Get, "/") {
                val token = oauthServer.issueToken()
                addHeader("Authorization", "Bearer ${token.serialize()}")
            }) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `Requests with valid token and correct scope should should return 200-Ok`() {
        withHopsTestApplication {
            with(handleRequest(Get, "/") {
                val token = oauthServer.issueToken(claims = mapOf("scope" to "nav:helse/v1/helseopplysninger"))
                addHeader("Authorization", "Bearer ${token.serialize()}")
            }) {
                assertEquals(OK, response.status())
            }
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