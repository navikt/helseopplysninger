package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val job = CoroutineScope(context).launch {
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

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    suspend fun process(message: Bundle) {
        // TODO: Publish resources to the HAPI fhir server
        val operationOutcome = validator.validate(message)

        if (!operationOutcome.isAllOk()) {
            val requestMessageHeader = message.entry.first().resource as MessageHeader
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
        id = IdentityGenerator.CreateUUID5(requestMessageHeader.id, "details").toString()
    }

    val responseMessageHeader = MessageHeader().apply {
        id = IdentityGenerator.CreateUUID5(requestMessageHeader.id, "response").toString()
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
        addResource(outcomeCopy)
    }
}

private fun asDestination(src: MessageHeader.MessageSourceComponent) =
    MessageHeader.MessageDestinationComponent(src.endpointElement).apply { name = src.name }

private fun asSource(dest: MessageHeader.MessageDestinationComponent) =
    MessageHeader.MessageSourceComponent(dest.endpointElement).apply { name = dest.name }
