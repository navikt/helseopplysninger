package dialogmelding

import dialogmelding.testutils.MQConsumer
import dialogmelding.testutils.Mocks
import dialogmelding.testutils.TestContainerFactory
import dialogmelding.testutils.readResourcesFile
import dialogmelding.testutils.readResourcesFileAsString
import dialogmelding.testutils.withTestApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.fail

@Testcontainers
class AppIntegrationTest {

    // Denne testen fungerer ikke pga: DetailedIllegalStateRuntimeException:
    // JMSWMQ0018: Failed to connect to queue manager 'QM1' with connection mode 'Client' and host name 'Client'.
    // Siden prosjektet er lagt på is har det ikke blitt brukt mer tid på å løse dette.
    @Test
    @EnabledIfEnvironmentVariable(named = "USE_TESTCONTAINERS", matches = "true")
    fun `fhir-message on Kafka, create pdf, sent on MQ`() {
        try {
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
        } catch (ex: Exception) {
            fail(ex)
        }
    }

    @Container
    val mqContainer = TestContainerFactory.ibmMq()
}
