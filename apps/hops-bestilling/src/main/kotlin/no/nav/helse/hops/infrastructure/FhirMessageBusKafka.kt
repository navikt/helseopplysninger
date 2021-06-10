package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.domain.FhirMessageBus
import no.nav.helse.hops.fhir.resources
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource
import java.time.Duration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FhirMessageBusKafka(
    private val consumer: Consumer<Unit, IBaseResource>,
    private val producer: Producer<Unit, IBaseResource>,
    private val config: Configuration.Kafka,
) : FhirMessageBus {
    override suspend fun publish(message: Bundle) {
        suspendCoroutine<RecordMetadata> { continuation ->
            val callback = Callback { metadata, exception ->
                if (metadata == null) {
                    continuation.resumeWithException(exception!!)
                } else {
                    continuation.resume(metadata)
                }
            }

            producer.send(ProducerRecord(config.topic, message), callback)
        }
    }

    override fun poll(): Flow<Bundle> =
        flow {
            try {
                consumer.subscribe(listOf(config.topic))

                while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                    val records = consumer.poll(Duration.ofSeconds(1))

                    // Needed to be cancellable, see: https://kotlinlang.org/docs/flow.html#flow-cancellation-basics
                    kotlinx.coroutines.delay(1)

                    // See https://www.hl7.org/fhir/messaging.html
                    records
                        .mapNotNull { it.value() as? Bundle }
                        .filter { it.type == Bundle.BundleType.MESSAGE && it.resources<Resource>().firstOrNull() is MessageHeader }
                        .forEach { emit(it) }
                }
            } finally {
                consumer.unsubscribe()
            }
        }
}
