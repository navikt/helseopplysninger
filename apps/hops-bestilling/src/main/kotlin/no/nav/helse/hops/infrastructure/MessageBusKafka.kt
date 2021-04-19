package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.MessageBus
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle

class MessageBusKafka(
    private val producer: Producer<Unit, IBaseResource>,
    private val config: Configuration.Kafka,
) : MessageBus {
    override suspend fun publish(message: Bundle) {
        val future = producer.send(ProducerRecord(config.topic, message))
        future.get()
    }
}
