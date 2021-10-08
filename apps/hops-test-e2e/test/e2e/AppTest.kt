package e2e

import e2e.Mocks.Dispatcher.respond
import e2e.Mocks.Matcher.get
import e2e._common.Results
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalSerializationApi
class AppTest : FeatureSpec({
    feature("GET /runTests") {
        scenario("happy path for all e2e tests") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val content = Json.decodeFromString<Results>(response.content!!)
                    content.failedTests shouldHaveSize 0
                    content.totalDurationMs shouldNotBe "0ms"
                }
            }
        }

        scenario("one test fails") {
            withTestApp {
                // Fail liveness
                Mocks.api.matchRequest(get("/actuator/live"), respond(503))

                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val content = Json.decodeFromString<Results>(response.content!!)
                    content.failedTests shouldHaveSize 1
                    content.failedTests.first().name shouldBe "api liveness"
                    Duration.parse(content.totalDurationMs) shouldBeGreaterThan Duration.milliseconds(0)
                }

                // Set mock back to happy path
                Mocks.api.matchRequest(get(LIVENESS_PATH), respond("live"))
            }
        }

        scenario("run test twice in a row") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    Json.decodeFromString<Results>(response.content!!).failedTests shouldHaveSize 0
                }
                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    Json.decodeFromString<Results>(response.content!!).failedTests shouldHaveSize 0
                }
            }
        }
    }
})
