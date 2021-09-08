package e2e

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest

class RunTestsTest : FeatureSpec({
    feature("POST /runTests") {
        scenario("run all tests and report with a repository dispatch event") {
            withTestApp {
                with(handleRequest(HttpMethod.Post, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.Accepted
                    response shouldHaveContent "Tests are now running.."
                }
            }
        }
    }
})
