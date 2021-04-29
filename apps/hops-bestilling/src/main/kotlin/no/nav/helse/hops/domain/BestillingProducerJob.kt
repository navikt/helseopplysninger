package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.infrastructure.Configuration
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Task
import org.hl7.fhir.r4.model.UrlType
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class BestillingProducerJob(
    private val messageBus: FhirMessageBus,
    private val messagingConfig: Configuration.FhirMessaging,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = CoroutineScope(context).launch {
        val message = createFhirMessage()
        messageBus.publish(message)
    }

    override fun close() {
        runBlocking {
            job.join()
        }
    }

    private fun createFhirMessage(): Bundle {
        val eventType = Coding("nav:hops:eventType", "bestilling", "Bestilling")
        val messageHeader = MessageHeader(eventType, MessageHeader.MessageSourceComponent(UrlType("")))
        messageHeader.destination = listOf(MessageHeader.MessageDestinationComponent(UrlType(messagingConfig.endpoint)))
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
