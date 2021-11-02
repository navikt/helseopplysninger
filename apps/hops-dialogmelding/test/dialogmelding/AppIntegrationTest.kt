package dialogmelding

import dialogmelding.testutils.Mocks
import dialogmelding.testutils.TestContainerFactory
import dialogmelding.testutils.readResourcesFile
import dialogmelding.testutils.withTestApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

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

                        delay(10000)
                    }
                }
            }
        }
    }

    @Container
    val mqContainer = TestContainerFactory.ibmMq()
}
