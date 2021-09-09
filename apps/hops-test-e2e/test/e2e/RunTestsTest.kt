package e2e

import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
class RunTestsTest : FeatureSpec({
    feature("GET /runTests") {
        scenario("run all tests and report back") {
            withTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/runTests") {
                        addHeader("Content-Type", "application/json")
                        addHeader("appName", "test")
                        addHeader("workflowId", "23")
                        addHeader("testScope", "ALL")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val results = Json.decodeFromString<Results>(response.content!!)
                    results.failedTests shouldHaveSize 1
                    results.failedTests.first().name shouldBe "GET ${Mocks.fileshare.getBaseUrl()}/isAlive"
                }
            }
        }
    }
})
