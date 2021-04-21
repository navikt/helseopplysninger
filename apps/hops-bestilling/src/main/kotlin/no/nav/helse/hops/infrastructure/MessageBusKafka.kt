package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.parser.DataFormatException
import no.nav.helse.hops.domain.MessageBus
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.slf4j.Logger
import java.time.Duration

class MessageBusKafka(
    private val consumer: Consumer<Unit, IBaseResource>,
    private val producer: Producer<Unit, IBaseResource>,
    private val config: Configuration.Kafka,
    private val logger: Logger,
) : MessageBus {
    init {
        consumer.subscribe(listOf(config.topic))
    }

    override suspend fun publish(message: Bundle) {
        val future = producer.send(ProducerRecord(config.topic, message))
        future.get()
    }

    override suspend fun poll(): List<Bundle> {
        try {
            val records = consumer.poll(Duration.ofSeconds(1))
            return records.mapNotNull { it.value() as? Bundle }
        } catch (ex: DataFormatException) {
            logger.error("Unable to parse received message on topic={}, error={}", config.topic, ex.message)
        }

        return emptyList()
    }
}
