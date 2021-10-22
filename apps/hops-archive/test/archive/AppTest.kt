package archive

import archive.testUtils.HOPS_TOPIC
import archive.testUtils.Mocks
import archive.testUtils.readResourcesFile
import archive.testUtils.withTestApp
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AppTest {
    @Test
    fun `isAlive returns 200 OK`() {
        Mocks().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, "/actuator/alive")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }

    @Test
    fun `fhir-message on Kafka, sent to archive`() {
        Mocks().use {
            withTestApp(it) {
                runBlocking {
                    withTimeout(10000) {
                        val message = readResourcesFile("/FhirMessage.json")
                        it.kafka.produce(HOPS_TOPIC, UUID.randomUUID(), message)

                        val req = it.dokarkiv.receivedRequest.await()
                        val expectedBody = readResourcesFile("/Expected.json")

                        val mapper = ObjectMapper()

                        assertEquals(mapper.readTree(expectedBody), mapper.readTree(req.body))
                        assertTrue(req.call.parameters.contains("forsoekFerdigstill", "true"))
                    }
                }
            }
        }
    }
}