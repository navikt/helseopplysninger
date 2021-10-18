package e2e

import mu.KotlinLogging
import no.nav.common.KafkaEnvironment
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.apache.kafka.common.serialization.UUIDSerializer
import java.util.UUID

const val HOPS_TOPIC = "helseopplysninger.river"

private val log = KotlinLogging.logger {}

object EmbeddedKafka {
    private val kafka: KafkaEnvironment = KafkaEnvironment(
        topicNames = listOf(HOPS_TOPIC),
    )

    private val producer = kafka.createProducer<UUID, ByteArray>()

    fun produce(topic: String, key: UUID, value: ByteArray, headers: List<Header>) {
        val record = ProducerRecord(topic, null, key, value, headers)
        producer.send(record).get().also {
            log.info("Produced record on $it (topic-partition@offset)")
            log.info("[key:$key] [value:${String(value)}] [header:${headers.map { h -> h.key() to String(h.value()) }}]")
        }
    }

    fun start() = kafka.start()
    fun shutdown() = kafka.tearDown()
    fun getHost() = kafka.brokersURL
}

private fun <K, V> KafkaEnvironment.createProducer() = KafkaProducer<K, V>(
    mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to brokersURL,
        CommonClientConfigs.CLIENT_ID_CONFIG to "hops-test-e2e-test",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to UUIDSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
    )
)

private fun <K, V> KafkaEnvironment.createConsumer() = KafkaConsumer<K, V>(
    mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to brokersURL,
        CommonClientConfigs.CLIENT_ID_CONFIG to "hops-test-e2e-test",
        ConsumerConfig.GROUP_ID_CONFIG to "hops-test-e2e-test",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to UUIDDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
    )
)
