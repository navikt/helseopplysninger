package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.MessageBus
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import java.time.Duration

class MessageBusKafka(
    private val consumer: Consumer<Unit, IBaseResource>,
    private val producer: Producer<Unit, IBaseResource>,
    private val config: Configuration.Kafka,
) : MessageBus {
    init {
        consumer.subscribe(listOf(config.topic))
    }

    override suspend fun publish(message: Bundle) {
        val future = producer.send(ProducerRecord(config.topic, message))
        future.get()
    }

    override suspend fun poll(): List<Bundle> {
        val records = consumer.poll(Duration.ofSeconds(1))

        // See https://www.hl7.org/fhir/messaging.html
        val messages = records
            .mapNotNull { it.value() as? Bundle }
            .filter { it.type == Bundle.BundleType.MESSAGE && it.entry?.firstOrNull()?.resource is MessageHeader }

        // For some reason the HAPI's json parser replaces all resource.id with entry.fullUrl.
        val resources = messages.flatMap { bundle -> bundle.entry.map { it.resource } }
        resources.forEach { it.id = it.id?.removePrefix("urn:uuid:") }

        return messages
    }
}
