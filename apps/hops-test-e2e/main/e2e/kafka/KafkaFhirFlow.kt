package e2e.kafka

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.plugin.logConsumed
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.time.Duration
import java.util.UUID

internal class KafkaFhirFlow(
    private val consumer: KafkaConsumer<UUID, ByteArray>,
    private val topic: String,
) : Closeable {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    suspend fun poll(): Flow<FhirMessage> = flow {
        consumer.subscribe(listOf(topic))

        runCatching {
            while (currentCoroutineContext().isActive) {
                consumer.poll(sec1.duration)
                    .filterNotNull()
                    .logConsumed(log)
                    .map(FhirMessage::fromRecord)
                    .forEach { emit(it) }
            }
        }

        consumer.unsubscribe()
    }

    override fun close() = runBlocking {
        keepUpToDate.cancelAndJoin()
    }

    private val sec1 = 1_000L
    private val Long.duration: Duration get() = Duration.ofMillis(this)

    private val keepUpToDate = CoroutineScope(Dispatchers.Default).launch {
        while (isActive) runCatching {
            poll()
        }.onFailure {
            log.error("Error while reading topic", it)
            if (it is CancellationException) throw it
            delay(sec1)
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
