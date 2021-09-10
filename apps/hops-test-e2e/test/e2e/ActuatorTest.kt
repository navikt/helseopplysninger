package e2e

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest

internal class ActuatorTest : FeatureSpec({
    feature("actuators") {
        scenario("GET isAlive = 200 OK") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/actuator/live")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "live"
                }
            }
        }

        scenario("GET isReady = 200 OK") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/actuator/ready")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "ready"
                }
            }
        }

        scenario("GET prometheus = 200 OK") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/metrics")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content shouldNotBe ""
                }
            }
        }
    }
})
