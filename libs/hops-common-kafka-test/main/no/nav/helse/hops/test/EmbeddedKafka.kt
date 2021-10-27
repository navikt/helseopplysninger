package no.nav.helse.hops.test

import no.nav.helse.hops.plugin.KafkaConfig
import no.nav.helse.hops.plugin.KafkaFactory
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import java.util.UUID

private val log = LoggerFactory.getLogger("test")
private val header = RecordHeader("Content-Type", "application/fhir+json;fhirVersion=4.0".toByteArray())

class EmbeddedKafka(topic: String) : AutoCloseable {
    private val kafka = ThreadSafeKafkaEnvironment(topicNames = listOf(topic), autoStart = true)
    private val producer = kafka.createProducer()
    val brokersURL get() = kafka.brokersURL

    fun produce(topic: String, key: UUID, value: ByteArray) {
        val record = ProducerRecord(topic, null, key, value, listOf(header))
        producer.send(record).get().also {
            log.info("Produced record on $it (topic-partition@offset)")
            log.info(record.toString())
        }
    }

    override fun close() {
        producer.close()
        kafka.close()
    }
}

private fun ThreadSafeKafkaEnvironment.createProducer(): Producer<UUID, ByteArray> {
    val config = KafkaConfig(
        brokers = brokersURL,
        groupId = "",
        topic = "",
        clientId = "producer-test-client",
        security = false,
        truststorePath = "",
        keystorePath = "",
        credstorePsw = ""
    )

    return KafkaFactory.createFhirProducer(config)
}
