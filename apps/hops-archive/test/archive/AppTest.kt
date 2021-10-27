package archive

import archive.testutils.Mocks
import archive.testutils.readResourcesFile
import archive.testutils.withTestApp
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
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
    fun `fhir-message on Kafka, create pdf, sent to archive`() {
        Mocks().use {
            withTestApp(it) {
                runBlocking {
                    withTimeout(10000) {
                        val message = readResourcesFile("/FhirMessage.json")
                        it.kafka.produce("helseopplysninger.river", UUID.randomUUID(), message)

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
