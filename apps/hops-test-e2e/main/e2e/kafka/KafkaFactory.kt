package e2e.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import java.util.UUID

internal object KafkaFactory {
    fun createConsumer(config: KafkaConfig.Kafka): KafkaConsumer<UUID, ByteArray> = when (config.security) {
        true -> KafkaConsumer(consumerConfig(config) + sslConfig(config))
        false -> KafkaConsumer(consumerConfig(config))
    }

    private fun consumerConfig(config: KafkaConfig.Kafka) = mapOf(
        ConsumerConfig.GROUP_ID_CONFIG to config.groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to UUIDDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to config.brokers,
        CommonClientConfigs.CLIENT_ID_CONFIG to config.clientId,
    )

    private fun sslConfig(config: KafkaConfig.Kafka) = mapOf(
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

data class KafkaConfig(val kafka: Kafka) {
    data class Kafka(
        val brokers: String,
        val groupId: String,
        val clientId: String,
        val security: Boolean,
        val truststorePath: String,
        val keystorePath: String,
        val credstorePsw: String,
        val topic: Topic,
    )

    data class Topic(val published: String)
}
