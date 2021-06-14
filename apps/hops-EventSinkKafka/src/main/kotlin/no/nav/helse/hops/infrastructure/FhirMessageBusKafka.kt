package no.nav.helse.hops.infrastructure

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.domain.FhirMessageBus
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration
import java.util.UUID

class FhirMessageBusKafka(
    private val consumer: Consumer<Unit, ByteArray>,
    private val config: Configuration.Kafka,
) : FhirMessageBus {
    override fun poll(): Flow<FhirMessage> =
        flow {
            try {
                consumer.subscribe(listOf(config.topic))

                while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                    val records = consumer.poll(Duration.ofSeconds(1))

                    // Needed to be cancellable, see: https://kotlinlang.org/docs/flow.html#flow-cancellation-basics
                    kotlinx.coroutines.delay(1)

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

private const val jsonR4 = "application/fhir+json; fhirVersion=4.0"

private fun toFhirMessage(record: ConsumerRecord<Unit, ByteArray>): FhirMessage {
    val requestIdHeader = record.headers().lastHeader(HttpHeaders.XRequestId)
    val requestId = requestIdHeader?.value()?.decodeToString() ?: UUID.randomUUID().toString()
    val contentType = record.headers().lastHeader(HttpHeaders.ContentType)?.value()?.decodeToString() ?: jsonR4

    return FhirMessage(record.value(), contentType, requestId)
}
