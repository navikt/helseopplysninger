package infrastructure

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.VoidDeserializer

object KafkaFactory {
    fun createFhirConsumer(config: EventSinkConfig.Kafka): Consumer<Unit, ByteArray> =
        when (config.security) {
            true -> KafkaConsumer(consumerConfig(config) + sslConfig(config))
            false -> KafkaConsumer(consumerConfig(config))
        }

    private fun consumerConfig(config: EventSinkConfig.Kafka) = mapOf(
        ConsumerConfig.GROUP_ID_CONFIG to config.groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to config.brokers,
        CommonClientConfigs.CLIENT_ID_CONFIG to config.clientId,
    )

    private fun sslConfig(config: EventSinkConfig.Kafka) = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to config.truststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to config.credstorePsw,
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to config.keystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to config.credstorePsw,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to config.credstorePsw,
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "",
    )
}
