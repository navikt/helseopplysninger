package e2e

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest

internal class ActuatorsTest : FeatureSpec({
    feature("actuators") {
        scenario("GET /actuator/live = 200 OK") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/actuator/live")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "live"
                }
            }
        }

        scenario("GET /actuator/ready = 200 OK") {
            withTestApp {
                with(handleRequest(HttpMethod.Get, "/actuator/ready")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "ready"
                }
            }
        }

        scenario("GET /metrics = 200 OK") {
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
