package questionnaire.routes

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import questionnaire.MockServers
import questionnaire.withHopsTestApplication
import kotlin.test.assertEquals

@DisplayName("POST /fhir/4.0/\$process-message")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockServers.Setup::class)
internal class FhirRoutesPostTest {
    @Test
    fun `without token should reject with 401`() {
        withHopsTestApplication {
            with(handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message")) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `with incorrect scope (claims) should reject with 401`() {
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                    val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.READ))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `without scope (claims) should reject with 401`() {
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                    val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf())
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `with valid tokens and correct claims should return 200`() {
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                    val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.WRITE))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                    setBody("""{}""")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }
}
