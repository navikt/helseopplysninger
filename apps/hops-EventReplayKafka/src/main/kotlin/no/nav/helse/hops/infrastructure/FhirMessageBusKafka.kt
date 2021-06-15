package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.domain.FhirMessageBus
import no.nav.helse.hops.fhir.idAsUUID
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.hl7.fhir.instance.model.api.IBaseResource
import java.time.Duration
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FhirMessageBusKafka(
    private val producer: Producer<UUID, IBaseResource>,
    private val consumer: Consumer<UUID, Unit>,
    private val config: Configuration.Kafka,
) : FhirMessageBus {
    override suspend fun publish(message: FhirMessage) {
        suspendCoroutine<RecordMetadata> { continuation ->
            val callback = Callback { metadata, exception ->
                if (metadata == null) continuation.resumeWithException(exception!!)
                else continuation.resume(metadata)
            }

            val record = createRecord(config.topic, message)
            producer.send(record, callback)
        }
    }

    /**
     * Fetches the latest message in all Partitions of the Topic and returns the highest HOPS-Message-Offset value.
     * This Offset represents the offset of an Event in the EventStore and is not necessarily equal to the Kafka-offset.
     * **/
    override suspend fun sourceOffsetOfLatestMessage(): Long =
        consumer.use { // Only used on startup and can therefore be closed.
            val partitionInfos = consumer.partitionsFor(config.topic) ?: emptyList()
            val topicPartitions = partitionInfos.map { TopicPartition(it.topic(), it.partition()) }

            consumer.assign(topicPartitions)
            consumer.seekToEnd(topicPartitions)

            val records = consumer.poll(Duration.ofSeconds(2))
            val hopsHeaders = records.map { HopsKafkaHeaders(it.headers()) }

            return hopsHeaders.map { it.offset }.maxOrNull() ?: 0
        }
}

private fun createRecord(topic: String, message: FhirMessage) =
    ProducerRecord<UUID, IBaseResource>(
        topic,
        null,
        message.content.timestamp.time,
        message.content.entry.first().resource.idAsUUID(), // MessageHeader.id
        message.content,
        HopsKafkaHeaders(message).kafkaHeaders
    )
