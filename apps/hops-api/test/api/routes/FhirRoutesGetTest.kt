package api.routes

import api.MockServers
import api.withHopsTestApplication
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.handleRequest
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@DisplayName("GET /fhir/4.0/Bundle")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockServers.Setup::class)
internal class FhirRoutesGetTest {
    @Test
    fun `without token should reject with 401`() {
        withHopsTestApplication {
            with(handleRequest(Get, "/fhir/4.0/Bundle")) {
                assertEquals(Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `with incorrect scope (claims) should reject with 401`() {
        withHopsTestApplication {
            with(
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.WRITE))
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
                handleRequest(Get, "/fhir/4.0/Bundle") {
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
                handleRequest(Get, "/fhir/4.0/Bundle") {
                    val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.READ))
                    addHeader("Authorization", "Bearer ${token.serialize()}")
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }
}
