package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.MessageBusProducer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MessageBusProducerKafka(
    private val producer: Producer<Unit, IBaseResource>,
    private val config: Configuration.Kafka,
) : MessageBusProducer {
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
}