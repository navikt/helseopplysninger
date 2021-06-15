package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.domain.FhirMessageBus
import no.nav.helse.hops.fhir.idAsUUID
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.hl7.fhir.instance.model.api.IBaseResource
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FhirMessageBusKafka(
    private val producer: Producer<UUID, IBaseResource>,
    private val config: Configuration.Kafka,
) : FhirMessageBus {
    override suspend fun publish(message: FhirMessage) {
        suspendCoroutine<RecordMetadata> { continuation ->
            val callback = Callback { metadata, exception ->
                if (metadata == null) continuation.resumeWithException(exception!!)
                else continuation.resume(metadata)
            }

            val record = createRecord(config.topic, message)
            producer.send(record, callback)
        }
    }
}

private fun createRecord(topic: String, message: FhirMessage) =
    ProducerRecord<UUID, IBaseResource>(
        topic,
        null,
        message.content.timestamp.time,
        message.content.entry.first().resource.idAsUUID(), // MessageHeader.id
        message.content,
        HopsKafkaHeaders(message).kafkaHeaders
    )
