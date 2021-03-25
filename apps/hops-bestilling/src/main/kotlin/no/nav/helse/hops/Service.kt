package no.nav.helse.hops

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Task
import org.hl7.fhir.r4.model.UrlType
import org.slf4j.Logger
import java.time.Duration

class Service(
    private val producer: Producer<Unit, IBaseResource>,
    private val consumer: Consumer<Unit, IBaseResource>,
    private val logger: Logger
) {

    fun execute() {
        for (i in 0..4) {
            val msg = createFhirMessage()

            val future = producer.send(ProducerRecord("Topic1", msg))
            future.get()
        }

        consumer.subscribe(listOf("Topic1"))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            logger.info("Consumed ${records.count()} records")

            records.forEach {
                val resource = it.value()
                logger.info("Message: ${resource.toJson()}")
            }

            if (!records.isEmpty) {
                break
            }
        }
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
}
