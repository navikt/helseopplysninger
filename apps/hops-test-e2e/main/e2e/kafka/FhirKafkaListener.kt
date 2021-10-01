package e2e.kafka

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.time.Duration
import java.util.UUID

internal class FhirKafkaListener(
    private val consumer: KafkaConsumer<UUID, ByteArray>,
    private val topic: String,
) : Closeable {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    suspend fun poll(): Flow<FhirMessage> = flow {
        consumer.subscribe(listOf(topic))

        runCatching {
            while (true) consumer.poll(sec1.duration)
                .filterNotNull()
                .map(FhirMessage::fromRecord)
                .forEach {
                    log.info("Consumed record: $it")
                    emit(it)
                }
        }

        consumer.unsubscribe()
    }

    override fun close() = runBlocking {
        keepUpToDate.cancel("test complete.")
    }

    private val sec1 = 1_000L
    private val Long.duration: Duration get() = Duration.ofMillis(this)

    private val keepUpToDate = CoroutineScope(Dispatchers.IO).launch {
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
