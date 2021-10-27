package e2e

import e2e.Mocks.Dispatcher.respond
import e2e.Mocks.Matcher.get
import e2e._common.Results
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.helse.hops.test.EmbeddedKafka
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ExperimentalTime
@ExperimentalSerializationApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    @BeforeAll
    fun setUp() {
        Mocks.kafka = EmbeddedKafka("helseopplysninger.river")
        Mocks.maskinporten.start()
        Mocks.api.start()
        Mocks.eventreplay.start()
        Mocks.eventsink.start()
        Mocks.eventstore.start()
    }

    @AfterAll
    fun teardown() {
        Mocks.kafka.close()
        Mocks.maskinporten.shutdown()
        Mocks.api.shutdown()
        Mocks.eventreplay.shutdown()
        Mocks.eventsink.shutdown()
        Mocks.eventstore.shutdown()
    }

    @Test
    fun `happy path for all e2e test`() {
        withTestApp {
            with(handleRequest(HttpMethod.Get, "/runTests")) {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = Json.decodeFromString<Results>(response.content!!)
                assertEquals(0, content.failedTests.size)
                assertNotEquals("0ms", content.totalDurationMs)
            }
        }
    }

    @Test
    fun `one test fails`() {
        withTestApp {
            // Fail liveness
            Mocks.api.matchRequest(get("/actuator/live"), respond(503))
            with(handleRequest(HttpMethod.Get, "/runTests")) {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = Json.decodeFromString<Results>(response.content!!)
                assertEquals(1, content.failedTests.size)
                assertEquals("api liveness", content.failedTests.first().name)
                assertTrue(Duration.parse(content.totalDurationMs) > Duration.milliseconds(0))
            }
        }

        // Set mock back to happy path
        Mocks.api.matchRequest(get(LIVENESS_PATH), respond("live"))
    }

    @Test
    fun `run test twice in a row`() {
        withTestApp {
            with(handleRequest(HttpMethod.Get, "/runTests")) {
                assertEquals(0, Json.decodeFromString<Results>(response.content!!).failedTests.size)
            }
            with(handleRequest(HttpMethod.Get, "/runTests")) {
                assertEquals(0, Json.decodeFromString<Results>(response.content!!).failedTests.size)
            }
        }
    }

    @Test
    fun `get liveness actuator returns 200 OK`() {
        withTestApp {
            with(handleRequest(HttpMethod.Get, "/actuator/live")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("live", response.content)
            }
        }
    }

    @Test
    fun `readiness actuator returns 200 OK`() {
        withTestApp {
            with(handleRequest(HttpMethod.Get, "/actuator/ready")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("ready", response.content)
            }
        }
    }

    @Test
    fun `metrics actuator returns 200 OK`() {
        withTestApp {
            with(handleRequest(HttpMethod.Get, "/actuator/metrics")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
                assertNotEquals("", response.content)
            }
        }
    }
}
