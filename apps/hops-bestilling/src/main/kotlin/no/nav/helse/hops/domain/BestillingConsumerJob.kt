package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.helse.hops.infrastructure.Configuration
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Reference
import org.slf4j.Logger
import java.io.Closeable
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class BestillingConsumerJob(
    private val messageBus: MessageBus,
    private val logger: Logger,
    private val validator: FhirResourceValidator,
    messagingConfig: Configuration.FhirMessaging,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val scope = CoroutineScope(context)

    init {
        scope.launch {
            while (isActive) {
                val messages = messageBus.poll()

                messages.forEach {
                    logger.debug("Message: ${it.toJson()}")

                    if (it.isMessageWithSingleDestination(messagingConfig.endpoint)) {
                        process(it)
                    }
                }
            }
        }
    }

    override fun close() {
        scope.cancel()
    }

    suspend fun process(message: Bundle) {
        // TODO: Publish resources to the HAPI fhir server
        val requestMessageHeader = message.entry[0].resource as MessageHeader
        val operationOutcome = validator.validate(message)

        if (!operationOutcome.isAllOk()) {
            val validationErrorResponse = createResponseMessage(requestMessageHeader, operationOutcome)
            messageBus.publish(validationErrorResponse)
        }
    }
}

private fun Bundle.isMessageWithSingleDestination(expectedDestination: String): Boolean {
    if (type == Bundle.BundleType.MESSAGE) {
        val header = entry.firstOrNull()?.resource as? MessageHeader ?: return false
        return header.destination.count() == 1 && header.destination.any { it.endpoint == expectedDestination }
    }

    return false
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
