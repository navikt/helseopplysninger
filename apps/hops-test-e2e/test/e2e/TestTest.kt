package e2e

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest

class TestTest : FeatureSpec({
    beforeSpec {
        Mocks.github.start()
        Mocks.internal.start()
    }

    afterSpec {
        Mocks.github.shutdown()
        Mocks.internal.shutdown()
    }

    feature("POST /runTests") {
        scenario("will run all tests and trigger a github repository dispatch event with the results") {
            withTestApp {
                with(handleRequest(HttpMethod.Post, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.Accepted
                    response shouldHaveContent "Tests are now running.."
                }
            }
        }

    }
})
