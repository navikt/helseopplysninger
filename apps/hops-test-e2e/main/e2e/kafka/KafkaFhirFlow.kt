package e2e.kafka

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.nav.helse.hops.plugin.logConsumed
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import java.time.Duration
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

private val log = KotlinLogging.logger {}
private const val sec1 = 1_000L
private const val millis100 = 100L

internal class KafkaFhirFlow(
    private val consumer: KafkaConsumer<UUID, ByteArray>,
    private val topic: String,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    private val job = CoroutineScope(Dispatchers.Default).launch {
        seekToLatestOffset()

        while (isActive) runCatching { poll() }
            .onFailure {
                log.error("Error while reading topic", it)
                if (it is CancellationException) throw it
                delay(millis100) // make it cancellable
            }
    }

    fun cancelFlow(): Boolean {
        job.cancel() // FIXME: what happens after this state? Will the app recover without restart
        error("Failed to asynchronically produce expected record")
    }

    suspend fun poll(predicate: (ConsumerRecord<UUID, ByteArray>) -> Boolean = { true }): Flow<FhirMessage> = flow {
        runCatching {
            while (true) {
                consumer.poll(sec1.duration)
                    .filterNotNull()
                    .filter(predicate)
                    .logConsumed(log)
                    .map(FhirMessage::fromRecord)
                    .forEach {
                        emit(it)
                    }
                delay(millis100) // make it cancellable
            }
        }

        consumer.unsubscribe()
    }

    private val Long.duration: Duration get() = Duration.ofMillis(this)

    fun seekToLatestOffset() {
        val partitionInfos = consumer.partitionsFor(topic) ?: emptyList()
        val topicPartitions = partitionInfos.map { TopicPartition(it.topic(), it.partition()) }
        consumer.assign(topicPartitions)
        consumer.endOffsets(topicPartitions).forEach { (topicPartition, endOffset) ->
            consumer.seek(topicPartition, max(endOffset - 1, 0))
            log.debug { "set offset on $topic (${topicPartition.partition()}) to ${max(endOffset - 1, 0)}" }
        }
    }
}

data class FhirMessage(val contentType: String, val content: String) {
    companion object {
        fun fromRecord(record: ConsumerRecord<UUID, ByteArray>) = FhirMessage(
            contentType = record.headerOrDefault(HttpHeaders.ContentType, ""),
            content = String(record.value(), Charsets.UTF_8)
        )

        private fun ConsumerRecord<UUID, ByteArray>.headerOrDefault(header: String, default: String) =
            headers().lastHeader(header)?.value()?.decodeToString() ?: default
    }
}
