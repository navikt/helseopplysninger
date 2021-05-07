package no.nav.helse.hops.fhir.messages

import no.nav.helse.hops.fhir.addResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import java.util.UUID

class OkResponseMessage(requestHeader: MessageHeader, responseId: UUID, data: List<Resource>) :
    BaseMessage(createBundle(requestHeader, responseId, data)) {
        val data: List<Resource> get() = bundle.entry.drop(1).map { it.resource as Resource }
    }

private fun createBundle(requestHeader: MessageHeader, responseId: UUID, data: List<Resource>): Bundle {
    val responseHeader = MessageHeader().apply {
        id = responseId.toString()
        event = requestHeader.event
        destination = listOf(asDestination(requestHeader.source))
        source = asSource(requestHeader.destination.single())
        response = MessageHeader.MessageHeaderResponseComponent().apply {
            identifier = requestHeader.id
            code = MessageHeader.ResponseType.OK
        }
        focus = data.map { Reference(it) }
    }

    return Bundle().apply {
        id = UUID.randomUUID().toString()
        timestampElement = InstantType.withCurrentTime()
        type = Bundle.BundleType.MESSAGE
        addResource(responseHeader)
        data.forEach { addResource(it) }
    }
}

private fun asDestination(src: MessageHeader.MessageSourceComponent) =
    MessageHeader.MessageDestinationComponent(src.endpointElement).apply { name = src.name }

private fun asSource(dest: MessageHeader.MessageDestinationComponent) =
    MessageHeader.MessageSourceComponent(dest.endpointElement).apply { name = dest.name }