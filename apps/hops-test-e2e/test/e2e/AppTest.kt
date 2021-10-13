package e2e

import e2e._common.Results
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalSerializationApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    @BeforeAll
    fun setUp() {
        EmbeddedKafka.start()
        Mocks.maskinporten.start()
        Mocks.api.start()
        Mocks.eventreplay.start()
        Mocks.eventsink.start()
        Mocks.eventstore.start()
    }

    @AfterAll
    fun teardown() {
        EmbeddedKafka.shutdown()
        Mocks.maskinporten.shutdown()
        Mocks.api.shutdown()
        Mocks.eventreplay.shutdown()
        Mocks.eventsink.shutdown()
        Mocks.eventstore.shutdown()
    }

    @Test
    fun `happy path for all e2e test`() {
        withTestApp {
            runBlocking {

                with(handleRequest(HttpMethod.Get, "/runTests")) {
                    response shouldHaveStatus HttpStatusCode.OK
                    val content = Json.decodeFromString<Results>(response.content!!)
                    content.failedTests shouldHaveSize 0
                    content.totalDurationMs shouldNotBe "0ms"
                }
            }
        }
    }
//
//    @Test
//    fun `one test fails`() {
//        withTestApp {
//            // Fail liveness
//            Mocks.api.matchRequest(get("/actuator/live"), respond(503))
//
//            with(handleRequest(HttpMethod.Get, "/runTests")) {
//                response shouldHaveStatus HttpStatusCode.OK
//                val content = Json.decodeFromString<Results>(response.content!!)
//                content.failedTests shouldHaveSize 1
//                content.failedTests.first().name shouldBe "api liveness"
//                Duration.parse(content.totalDurationMs) shouldBeGreaterThan Duration.milliseconds(0)
//            }
//
//            // Set mock back to happy path
//            Mocks.api.matchRequest(get(LIVENESS_PATH), respond("live"))
//        }
//    }
//
//    @Test
//    fun `run test twice in a row`() {
//        withTestApp {
//            with(handleRequest(HttpMethod.Get, "/runTests")) {
//                Json.decodeFromString<Results>(response.content!!).failedTests shouldHaveSize 0
//            }
//            with(handleRequest(HttpMethod.Get, "/runTests")) {
//                Json.decodeFromString<Results>(response.content!!).failedTests shouldHaveSize 0
//            }
//        }
//    }
//
//    @Test
//    fun `get liveness actuator returns 200 OK`() {
//        withTestApp {
//            with(handleRequest(HttpMethod.Get, "/actuator/live")) {
//                response shouldHaveStatus HttpStatusCode.OK
//                response shouldHaveContent "live"
//            }
//        }
//    }
//
//    @Test
//    fun `readiness actuator returns 200 OK`() {
//        withTestApp {
//            with(handleRequest(HttpMethod.Get, "/actuator/ready")) {
//                response shouldHaveStatus HttpStatusCode.OK
//                response shouldHaveContent "ready"
//            }
//        }
//    }
//
//    @Test
//    fun `metrics actuator returns 200 OK`() {
//        withTestApp {
//            with(handleRequest(HttpMethod.Get, "/metrics")) {
//                response shouldHaveStatus HttpStatusCode.OK
//                response.content shouldNotBe null
//                response.content shouldNotBe ""
//            }
//        }
//    }
}
