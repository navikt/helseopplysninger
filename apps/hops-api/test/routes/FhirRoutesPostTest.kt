package routes

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import withHopsTestApplication

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
                        val token = MockServers.oAuth.issueToken(claims = mapOf("scope" to "/test-subscribe"))
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
                        val token = MockServers.oAuth.issueToken()
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
                        val token = MockServers.oAuth.issueToken(claims = mapOf("scope" to "/test-publish"))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe HttpStatusCode.OK
                }
            }
        }
    }
})
