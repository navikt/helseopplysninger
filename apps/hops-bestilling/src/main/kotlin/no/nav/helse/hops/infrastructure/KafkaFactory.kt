package no.nav.helse.hops.infrastructure

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.VoidDeserializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.util.Properties

object KafkaFactory {
    fun createFhirProducer(config: Configuration.Kafka): Producer<Unit, IBaseResource> {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.brokers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaFhirResourceSerializer::class.java)
        }
        return KafkaProducer(props)
    }

    fun createFhirConsumer(config: Configuration.Kafka): Consumer<Unit, IBaseResource> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.brokers)
            put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaFhirResourceDeserializer::class.java)
        }
        return KafkaConsumer(props)
    }
}
