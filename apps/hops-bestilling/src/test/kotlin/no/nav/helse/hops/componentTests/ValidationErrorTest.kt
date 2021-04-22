package no.nav.helse.hops.componentTests

import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.domain.isAllOk
import no.nav.helse.hops.domain.toJson
import no.nav.helse.hops.infrastructure.FhirResourceValidatorHapi
import no.nav.helse.hops.infrastructure.KafkaFhirResourceSerializer
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.testUtils.ResourceLoader
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serializer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.OperationOutcome
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOPIC = "helseopplysninger.bestilling"
private const val PARTITION = 0

class ValidationErrorTest() : java.io.Closeable {
    private val consumerMock = createMockConsumer()
    private val producerMock =
        MockProducer(true, Serializer<Unit> { _, _ -> ByteArray(0) }, KafkaFhirResourceSerializer())
    private val koinApp: KoinApplication

    init {
        val testKoinModule = module(override = true) {
            single<Producer<Unit, IBaseResource>> { producerMock }
            single<Consumer<Unit, IBaseResource>> { consumerMock }
        }

        koinApp = startKoin {
            modules(KoinBootstrapper.singleModule, testKoinModule)
        }
    }

    @Test
    fun `invalid message should result in response with operation-outcome`() {
        val requestMessage = ResourceLoader.asFhirResource<Bundle>("/fhir/invalid-message-warning-on-name.json")
        consumerMock.schedulePollTask { consumerMock.addRecord(ConsumerRecord(TOPIC, PARTITION, 0, Unit, requestMessage)) }

        koinApp.close()

        assertEquals(1, producerMock.history().size)

        val responseMessage = producerMock.history().single().value() as Bundle
        assertEquals(2, responseMessage.entry.count())
        assertTrue(responseMessage.entry[0].resource is MessageHeader)
        assertTrue(responseMessage.entry[1].resource is OperationOutcome)

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

    override fun close() {
        consumerMock.close()
        producerMock.close()
    }
}

private fun createMockConsumer(): MockConsumer<Unit, IBaseResource> {
    return MockConsumer<Unit, IBaseResource>(OffsetResetStrategy.EARLIEST).apply {
        schedulePollTask {
            rebalance(listOf(TopicPartition(TOPIC, 0)))
        }

        val startOffsets = HashMap<TopicPartition, Long>()
        val tp = TopicPartition(TOPIC, PARTITION)
        startOffsets[tp] = 0L

        updateBeginningOffsets(startOffsets)
    }
}
