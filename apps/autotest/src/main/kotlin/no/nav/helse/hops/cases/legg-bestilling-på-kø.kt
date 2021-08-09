package no.nav.helse.hops.cases

import no.nav.helse.hops.fhir.createFhirMessage
import no.nav.helse.hops.utils.DockerComposeEnv
import no.nav.helse.hops.utils.KafkaFactory
import org.apache.kafka.clients.producer.ProducerRecord

fun leggBestillingPaKafka() {
    val config = DockerComposeEnv()

    val producer = KafkaFactory.createFhirProducer(config)
    val message = createFhirMessage()
    producer.send(ProducerRecord(config.bestillingTopic, message))
}
