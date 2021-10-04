package archive

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.convert.ContentTypes
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

class FhirMessage(val content: ByteArray, val contentType: String) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.isNotBlank()) { "ContentType cannot be blank." }
    }
}

class FhirMessageStream(
    private val consumer: Consumer<Unit, ByteArray>,
    private val config: Config.Kafka,
) {
    fun poll(): Flow<FhirMessage> =
        flow {
            try {
                consumer.subscribe(listOf(config.topic))

                while (currentCoroutineContext().isActive) {
                    val records = consumer.poll(Duration.ofSeconds(1))

                    records
                        .filter { it.value() != null && it.value().isNotEmpty() }
                        .map(::toFhirMessage)
                        .forEach { emit(it) }
                }
            } finally {
                consumer.unsubscribe()
            }
        }
}

private fun toFhirMessage(record: ConsumerRecord<Unit, ByteArray>): FhirMessage {
    fun valueOf(header: String) = record.headers().lastHeader(header)?.value()?.decodeToString()
    val contentType = valueOf(HttpHeaders.ContentType) ?: ContentTypes.fhirJsonR4.toString()

    return FhirMessage(record.value(), contentType)
}
