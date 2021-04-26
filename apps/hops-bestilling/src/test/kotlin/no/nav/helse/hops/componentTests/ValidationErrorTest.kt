package no.nav.helse.hops.componentTests

import ca.uhn.fhir.rest.client.api.IGenericClient
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.domain.isAllOk
import no.nav.helse.hops.domain.toJson
import no.nav.helse.hops.infrastructure.FhirResourceValidatorHapi
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.testUtils.KafkaMock
import no.nav.helse.hops.testUtils.ResourceLoader
import no.nav.helse.hops.testUtils.addFhirMessage
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.OperationOutcome
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationErrorTest {
    private val consumerMock = KafkaMock.createConsumer()
    private val producerMock = KafkaMock.createProducer()
    private val koinApp = startKoin {
        val testKoinModule = module(override = true) {
            single<Producer<Unit, IBaseResource>> { producerMock }
            single<Consumer<Unit, IBaseResource>> { consumerMock }
            single { mockk<IGenericClient>() }
        }

        modules(KoinBootstrapper.singleModule, testKoinModule)
    }

    @Test
    fun `invalid message should result in response with operation-outcome`() {
        val requestMessage = ResourceLoader.asFhirResource<Bundle>("/fhir/invalid-message-warning-on-name.json")
        consumerMock.addFhirMessage(requestMessage)

        koinApp.close()

        assertEquals(1, producerMock.history().size)

        val responseMessage = producerMock.history().single().value() as Bundle

        val resources = responseMessage.entry.map { it.resource }
        assertEquals(2, resources.count())
        assertTrue(resources[0] is MessageHeader)
        assertTrue(resources[1] is OperationOutcome)

        val expectedResponseMessage =
            ResourceLoader.asFhirResource<Bundle>("/fhir/invalid-message-warning-on-name-expected-response.json").apply {
                id = responseMessage.id
                timestamp = responseMessage.timestamp
                entry.forEach { it.resource.id = it.resource.id.removePrefix("urn:uuid:") }
            }

        assertEquals(expectedResponseMessage.toJson(), responseMessage.toJson())

        runBlocking {
            val outcome = FhirResourceValidatorHapi.validate(responseMessage)
            assertTrue(outcome.isAllOk(), outcome.toJson())
        }
    }
}
