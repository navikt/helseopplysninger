package e2e

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldNotBe
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication


internal class ActuatorTest : FeatureSpec({
    feature("actuators") {
        scenario("GET isAlive = 200 OK") {
            withTestApplication(Application::main) {
                with(handleRequest(HttpMethod.Get, "/isAlive")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "live"
                }
            }
        }

        scenario("GET isReady = 200 OK") {
            withTestApplication(Application::main) {
                with(handleRequest(HttpMethod.Get, "/isReady")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "ready"
                }
            }
        }

        scenario("GET prometheus = 200 OK") {
            withTestApplication(Application::main) {
                with(handleRequest(HttpMethod.Get, "/prometheus")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content shouldNotBe ""
                }
            }
        }
    }
})
