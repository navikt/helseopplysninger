import infrastructure.EVENT_STORE_CLIENT_NAME
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.testing.handleRequest
import no.nav.helse.hops.test.oAuthMock
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class GetFhirBundleTest : FeatureSpec({
    feature("GET /fhir/4.0/Bundle") {
        scenario("without token should reject with 401") {
            withHopsTestApplication {
                with(handleRequest(Get, "/fhir/4.0/Bundle")) {
                    response.status() shouldBe Unauthorized
                }
            }
        }

        scenario("with incorrect scope (claims) should reject with 401") {
            withHopsTestApplication {
                with(
                    handleRequest(Get, "/fhir/4.0/Bundle") {
                        val token = oAuthMock.issueToken(claims = mapOf("scope" to "/test-publish"))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe Unauthorized
                }
            }
        }

        scenario("without scope (claims) should reject with 401") {
            withHopsTestApplication {
                with(
                    handleRequest(Get, "/fhir/4.0/Bundle") {
                        val token = oAuthMock.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe Unauthorized
                }
            }
        }

        scenario("with valid tokens and correct claims should return 200") {
            val eventStoreModule = module { single(named(EVENT_STORE_CLIENT_NAME)) { createEventStoreMockClient() } }
            withHopsTestApplication(eventStoreModule) {
                with(
                    handleRequest(Get, "/fhir/4.0/Bundle") {
                        val token = oAuthMock.issueToken(claims = mapOf("scope" to "/test-subscribe"))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response.status() shouldBe OK
                }
            }
        }
    }
})
