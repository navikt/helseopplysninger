package eventreplay.infrastructure

import ca.uhn.fhir.rest.api.Constants
import eventreplay.domain.Constants.MessageHeaders.SOURCE_OFFSET
import eventreplay.domain.FhirMessage
import eventreplay.domain.FhirMessageBus
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import no.nav.helse.hops.plugin.send
import java.time.Duration
import java.time.ZoneOffset
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

class FhirMessageBusKafka(
    private val producer: Producer<UUID, ByteArray>,
    private val consumer: Consumer<UUID, ByteArray>,
    private val config: Config.Kafka,
) : FhirMessageBus {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override suspend fun publish(message: FhirMessage) {
        suspendCoroutine<RecordMetadata> { continuation ->
            val callback = Callback { metadata, exception ->
                if (metadata == null) continuation.resumeWithException(exception!!)
                else continuation.resume(metadata)
            }

            val record = createRecord(config.topic, message)
            producer.send(record, callback, log)
        }
    }

    /**
     * Fetches the latest message in all Partitions of the Topic and returns the highest HOPS-Message-Offset value.
     * This Offset represents the offset of an Event in the EventStore and is not necessarily equal to the Kafka-offset.
     * **/
    override suspend fun sourceOffsetOfLatestMessage(): Long {
        try {
            val partitionInfos = consumer.partitionsFor(config.topic) ?: emptyList()
            val topicPartitions = partitionInfos.map { TopicPartition(it.topic(), it.partition()) }

            consumer.assign(topicPartitions)
            consumer.endOffsets(topicPartitions).forEach { (topicPartition, endOffset) ->
                consumer.seek(topicPartition, max(endOffset - 1, 0))
            }

            val records = consumer.poll(Duration.ofSeconds(2))
            val sourceOffsets = records.map { it.headers()[SOURCE_OFFSET].toLong() }

            return sourceOffsets.map { it + 1 }.maxOrNull() ?: 0
        } finally {
            consumer.unsubscribe()
        }
    }
}

private fun createRecord(topic: String, message: FhirMessage) =
    ProducerRecord(
        topic,
        null,
        message.timestamp.toInstant(ZoneOffset.UTC).toEpochMilli(),
        message.id,
        message.content,
        RecordHeaders().also {
            it[Constants.HEADER_CONTENT_TYPE] = message.contentType
            it[SOURCE_OFFSET] = message.sourceOffset.toString()
        }
    )

private operator fun Headers.get(key: String) =
    headers(key).map { it.value().decodeToString() }.single()

private operator fun Headers.set(key: String, value: String) {
    remove(key).add(key, value.toByteArray())
}
