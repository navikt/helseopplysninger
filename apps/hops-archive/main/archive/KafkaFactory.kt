package archive

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import java.util.Properties
import java.util.UUID

object KafkaFactory {
    fun createFhirConsumer(config: Config.Kafka): Consumer<UUID, ByteArray> {
        val props = createCommonGcpKafkaProperties(config).also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = config.groupId
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = UUIDDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        }
        return KafkaConsumer(props)
    }

    private fun createCommonGcpKafkaProperties(config: Config.Kafka): Properties {
        return Properties().also {
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = config.brokers
            it[CommonClientConfigs.CLIENT_ID_CONFIG] = config.clientId

            if (config.security) {
                it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
                it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
                it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = config.truststorePath
                it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = config.credstorePsw
                it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
                it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = config.keystorePath
                it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = config.credstorePsw
                it[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = config.credstorePsw
                it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            }
        }
    }
}
