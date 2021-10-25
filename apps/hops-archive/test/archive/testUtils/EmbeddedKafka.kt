package archive.testUtils

import io.ktor.http.HttpHeaders
import no.nav.helse.hops.convert.ContentTypes
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import java.util.UUID
import no.nav.helse.hops.test.ThreadSafeKafkaEnvironment
import org.slf4j.LoggerFactory

const val HOPS_TOPIC = "helseopplysninger.river"
private val log = LoggerFactory.getLogger("test")
private val header = RecordHeader(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.toString().toByteArray())

class EmbeddedKafka : AutoCloseable {
    private val kafka = ThreadSafeKafkaEnvironment(
        topicNames = listOf(HOPS_TOPIC),
        autoStart = true,
    )

    private val producer = kafka.createProducer()

    fun produce(topic: String, key: UUID, value: ByteArray) {
        val record = ProducerRecord(topic, null, key, value, listOf(header))
        producer.send(record).get().also {
            log.info("Produced record on $it (topic-partition@offset)")
            log.info(record.toString())
        }
    }

    fun getHost() = kafka.brokersURL

    override fun close() {
        producer.close()
        kafka.close()
    }
}

private fun ThreadSafeKafkaEnvironment.createProducer() = KafkaProducer<UUID, ByteArray>(
    mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to brokersURL,
        CommonClientConfigs.CLIENT_ID_CONFIG to "hops-test-e2e-test",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to UUIDSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
    )
)
