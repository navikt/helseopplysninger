package no.nav.helse.hops.hops.fhir

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import org.hl7.fhir.r4.model.UrlType

fun Bundle.addResource(resource: Resource): Bundle.BundleEntryComponent {
    val entry = Bundle.BundleEntryComponent()
    entry.fullUrl = "urn:uuid:${resource.id}"
    entry.resource = resource
    addEntry(entry)
    if (resource.resourceType == MessageHeader().resourceType) {
        this.entry.reverse()
    }
    return entry
}

fun createFhirMessage(): Bundle {
    val eventType = Coding("nav:hops:eventType", "bestilling", "Bestilling")
    val messageHeader = MessageHeader(eventType, MessageHeader.MessageSourceComponent(UrlType("")))
    messageHeader.destination = listOf(MessageHeader.MessageDestinationComponent(UrlType("http://localhost:1234")))
    val task = Task()
    val questionnaire = Questionnaire()

    return Bundle().apply {
        type = Bundle.BundleType.MESSAGE
        addResource(messageHeader)
        addResource(task)
        addResource(questionnaire)
    }
}
