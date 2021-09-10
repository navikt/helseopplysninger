package e2e

import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

@ExperimentalSerializationApi
class AppTest : FeatureSpec({
    feature("GET /runTests") {
        scenario("happy path") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val content = Json.decodeFromString<Results>(response.content!!)
                    content.failedTests shouldHaveSize 0
                    content.totalDurationMs shouldNotBe "0md"
                }
            }
        }

        scenario("one service is not live") {
            withTestApp {
                HopsMocks.fileshare.matchRequest(
                    matcher = HopsMocks.matchActuatorLive(),
                    dispatcher = { MockResponse().setResponseCode(HttpStatusCode.NotFound.value) }
                )
                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val content = Json.decodeFromString<Results>(response.content!!)
                    content.failedTests shouldHaveSize 1
                    content.failedTests.first().name shouldBe "GET ${HopsMocks.fileshare.getBaseUrl()}/isAlive"
                    content.totalDurationMs shouldNotBe "0ms"
                }
            }
        }
    }
})
