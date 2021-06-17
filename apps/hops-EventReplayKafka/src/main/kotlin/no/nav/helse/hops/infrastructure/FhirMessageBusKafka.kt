package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.domain.FhirMessageBus
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import java.time.Duration
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

class FhirMessageBusKafka(
    private val producer: Producer<UUID, ByteArray>,
    private val consumer: Consumer<UUID, ByteArray>,
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
            consumer.endOffsets(topicPartitions).forEach { (topicPartition, endOffset) ->
                consumer.seek(topicPartition, max(endOffset - 1, 0))
            }

            val records = consumer.poll(Duration.ofSeconds(2))
            val hopsHeaders = records.map { HopsKafkaHeaders(it.headers()) }

            return hopsHeaders.map { it.sourceOffset }.maxOrNull() ?: 0
        }
}

private fun createRecord(topic: String, message: FhirMessage) =
    ProducerRecord<UUID, ByteArray>(
        topic,
        null,
        null, // message.content.timestamp.time,
        message.id,
        message.content,
        HopsKafkaHeaders(message).kafkaHeaders
    )
