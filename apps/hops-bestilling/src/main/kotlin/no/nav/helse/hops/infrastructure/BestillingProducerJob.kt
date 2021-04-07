package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import no.nav.helse.hops.domain.addResource
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Task
import org.hl7.fhir.r4.model.UrlType
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class BestillingProducerJob(
    private val producer: Producer<Unit, IBaseResource>,
    config: Configuration.Kafka,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val scope = CoroutineScope(context)

    init {
        scope.launch {
            val msg = createFhirMessage()
            val future = producer.send(ProducerRecord(config.topic, msg))
            future.get()
        }
    }

    override fun close() {
        scope.cancel()
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
