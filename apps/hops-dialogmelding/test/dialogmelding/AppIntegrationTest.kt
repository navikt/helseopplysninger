package dialogmelding

import dialogmelding.testutils.MQConsumer
import dialogmelding.testutils.Mocks
import dialogmelding.testutils.TestContainerFactory
import dialogmelding.testutils.readResourcesFile
import dialogmelding.testutils.readResourcesFileAsString
import dialogmelding.testutils.withTestApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import kotlin.test.assertEquals

@Testcontainers
class AppIntegrationTest {

    @Test
    @Disabled
    fun `fhir-message on Kafka, create pdf, sent on MQ`() {
        Mocks().use {
            withTestApp(it) {
                runBlocking {
                    withTimeout(10000) {
                        val message = readResourcesFile("/FhirMessage.json")
                        it.kafka.produce("helseopplysninger.river", UUID.randomUUID(), message)

                        val mqMessage = MQConsumer().receive()
                        val expectedBody = readResourcesFileAsString("/Expected.json")

                        assertEquals(expectedBody, mqMessage)
                    }
                }
            }
        }
    }

    @Container
    val mqContainer = TestContainerFactory.ibmMq()
}
