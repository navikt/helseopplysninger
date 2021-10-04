package archive.infrastructure

import archive.domain.FhirMessage
import archive.domain.FhirMessageBus
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.convert.ContentTypes
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

class FhirMessageBusKafka(
    private val consumer: Consumer<Unit, ByteArray>,
    private val config: Config.Kafka,
) : FhirMessageBus {
    override fun poll(): Flow<FhirMessage> =
        flow {
            try {
                consumer.subscribe(listOf(config.topic))

                while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
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
