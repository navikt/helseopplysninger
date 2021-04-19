package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Reference
import org.slf4j.Logger
import java.util.UUID

interface FhirMessageProcessor {
    suspend fun process(requestMessage: Bundle)
}

class FhirMessageProcessorImpl(
    private val validator: FhirResourceValidator,
    private val messageBus: MessageBus,
    private val logger: Logger
) : FhirMessageProcessor {
    override suspend fun process(requestMessage: Bundle) {
        // TODO: Publish resources to the HAPI fhir server
        logger.info("Message: ${requestMessage.toJson()}")

        val requestMessageHeader = requestMessage.entry.first().resource.copy() as MessageHeader
        val operationOutcome = validator.validate(requestMessage)

        if (!operationOutcome.okWithoutWarnings()) {
            val responseMessage = createResponseMessage(requestMessageHeader, operationOutcome)
            messageBus.publish(responseMessage)
        }
    }
}

private fun createResponseMessage(
    requestMessageHeader: MessageHeader,
    operationOutcome: OperationOutcome
): Bundle {
    val outcomeCopy = operationOutcome.copy().apply {
        id = "${requestMessageHeader.id}-outcome"
    }

    val responseMessageHeader = MessageHeader().apply {
        id = "${requestMessageHeader.id}-response"
        event = requestMessageHeader.event
        destination = listOf(asDestination(requestMessageHeader.source))
        source = asSource(requestMessageHeader.destination.single())
        response = MessageHeader.MessageHeaderResponseComponent().apply {
            identifier = requestMessageHeader.id
            code = MessageHeader.ResponseType.FATALERROR
            details = Reference(outcomeCopy)
        }
    }

    return Bundle().apply {
        id = UUID.randomUUID().toString()
        timestampElement = InstantType.withCurrentTime()
        type = Bundle.BundleType.MESSAGE
        addResource(responseMessageHeader)
        addResource(operationOutcome)
    }
}

private fun asDestination(src: MessageHeader.MessageSourceComponent) =
    MessageHeader.MessageDestinationComponent(src.endpointElement).apply { name = src.name }

private fun asSource(dest: MessageHeader.MessageDestinationComponent) =
    MessageHeader.MessageSourceComponent(dest.endpointElement).apply { name = dest.name }