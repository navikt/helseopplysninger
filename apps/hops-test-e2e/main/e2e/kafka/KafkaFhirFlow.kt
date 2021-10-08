package e2e.kafka

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import mu.KotlinLogging
import no.nav.helse.hops.plugin.logConsumed
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import java.time.Duration
import java.util.UUID
import kotlin.math.max

private val log = KotlinLogging.logger {}

internal class KafkaFhirFlow(
    private val consumer: KafkaConsumer<UUID, ByteArray>,
    private val topic: String,
) {
    suspend fun poll(): Flow<FhirMessage> = flow {
        runCatching {
            while (currentCoroutineContext().isActive) {
                consumer.poll(sec2.duration)
                    .filterNotNull()
                    .logConsumed(log)
                    .map(FhirMessage::fromRecord)
                    .forEach {
                        emit(it)
                    }
            }
        }

        consumer.unsubscribe()
    }

    private val sec2 = 2_000L
    private val Long.duration: Duration get() = Duration.ofMillis(this)

    fun seekToLatestOffset() {
        val partitionInfos = consumer.partitionsFor(topic) ?: emptyList()
        val topicPartitions = partitionInfos.map { TopicPartition(it.topic(), it.partition()) }
        consumer.assign(topicPartitions)
        consumer.endOffsets(topicPartitions).forEach { (topicPartition, endOffset) ->
            consumer.seek(topicPartition, max(endOffset - 1, 0))
            log.info { "set offset on $topic (${topicPartition.partition()}) to ${max(endOffset - 1, 0)}" }
        }
    }
}

data class FhirMessage(val contentType: String, val content: String) {
    companion object {
        fun fromRecord(record: ConsumerRecord<UUID, ByteArray>) = FhirMessage(
            contentType = record.headerOrDefault(HttpHeaders.ContentType, ""),
            content = String(record.value())
        )

        private fun ConsumerRecord<UUID, ByteArray>.headerOrDefault(header: String, default: String) =
            headers().lastHeader(header)?.value()?.decodeToString() ?: default
    }
}
