package no.nav.helse.hops

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.VoidDeserializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Task
import org.hl7.fhir.r4.model.UrlType
import java.time.Duration
import java.util.Properties

private fun createProducer(): Producer<Unit, IBaseResource> {
    val props = Properties().apply {
        put("bootstrap.servers", "localhost:9092")
        put("key.serializer", VoidSerializer::class.java)
        put("value.serializer", FhirResourceSerializer::class.java)
    }
    return KafkaProducer(props)
}

private fun createConsumer(): Consumer<Unit, IBaseResource> {
    val props = Properties().apply {
        put("bootstrap.servers", "localhost:9092")
        put("group.id", "hello-world")
        put("key.deserializer", VoidDeserializer::class.java)
        put("value.deserializer", FhirResourceDeserializer::class.java)
    }
    return KafkaConsumer(props)
}

private fun createFhirMessage(): Bundle {
    val eventType = Coding("nav:hops:eventType", "bestilling", "Bestilling")
    val messageHeader = MessageHeader(eventType, MessageHeader.MessageSourceComponent(UrlType("")))
    val task = Task()
    val questionnaire = Questionnaire()

    return Bundle().apply {
        type = Bundle.BundleType.MESSAGE
        addResource(messageHeader)
        addResource(task)
        addResource(questionnaire)
    }
}

fun main() {

    createProducer().use { producer ->
        for (i in 0..4) {
            val msg = createFhirMessage()

            val future = producer.send(ProducerRecord("Topic1", msg))
            future.get()
        }
    }

    createConsumer().use { consumer ->
        consumer.subscribe(listOf("Topic1"))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            println("Consumed ${records.count()} records")

            records.iterator().forEach {
                val resource = it.value()
                println("Message: ${resource.toJson()}")
            }

            if (!records.isEmpty) {
                break
            }
        }
    }
}