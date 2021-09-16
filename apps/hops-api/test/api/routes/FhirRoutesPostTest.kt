package api.routes

import api.MockServers
import api.withHopsTestApplication
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes

internal class FhirRoutesPostTest : FeatureSpec({
    feature("POST /fhir/4.0/\$process-message") {
        scenario("without token should reject with 401") {
            withHopsTestApplication {
                with(handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message")) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        scenario("with incorrect scope (claims) should reject with 401") {
            withHopsTestApplication {
                with(
                    handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.READ))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        scenario("without scope (claims) should reject with 401") {
            withHopsTestApplication {
                with(
                    handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf())
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        scenario("with valid tokens and correct claims should return 200") {
            withHopsTestApplication {
                with(
                    handleRequest(HttpMethod.Post, "/fhir/4.0/\$process-message") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.WRITE))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        setBody("""{}""")
                    }
                ) {
                    response.status() shouldBe HttpStatusCode.OK
                }
            }
        }
    }
})
