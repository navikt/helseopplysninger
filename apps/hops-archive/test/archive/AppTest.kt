package archive

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.server.testing.handleRequest
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AppTest {
    @Test
    fun `isAlive returns 200 OK`() {
        Mocks().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, "/internal/isAlive")) {
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
                    it.kafka.produce(HOPS_TOPIC, UUID.randomUUID(), ByteArray(0), emptyList())
                    val call = it.dokarkiv.receivedRequest.await()
                    val requestBody = call.receiveText()
                }
            }
        }
    }
}
