package no.nav.helse.hops.testUtils

import no.nav.helse.hops.infrastructure.KafkaFhirResourceSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serializer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle

object KafkaMock {
    const val TOPIC = "helseopplysninger.bestilling"
    const val PARTITION = 0

    fun createConsumer(): MockConsumer<Unit, IBaseResource> {
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

    fun createProducer() =
        MockProducer(true, Serializer<Unit> { _, _ -> ByteArray(0) }, KafkaFhirResourceSerializer())
}

fun MockConsumer<Unit, IBaseResource>.addFhirMessage(msg: Bundle) {
    schedulePollTask { addRecord(ConsumerRecord(KafkaMock.TOPIC, KafkaMock.PARTITION, 0, Unit, msg) ) }
}