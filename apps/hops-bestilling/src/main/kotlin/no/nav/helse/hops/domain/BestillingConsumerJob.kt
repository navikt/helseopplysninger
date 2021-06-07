package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.fhir.addResource
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.infrastructure.Configuration
import no.nav.helse.hops.toUri
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger
import java.io.Closeable
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class BestillingConsumerJob(
    private val messageBus: FhirMessageBus,
    private val logger: Logger,
    private val validator: FhirResourceValidator,
    private val fhirRepo: FhirRepository,
    messagingConfig: Configuration.FhirMessaging,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = messageBus
        .poll()
        .onEach { logger.debug("Message: ${it.toJson()}") }
        .filter { it.hasDestination(messagingConfig.endpoint) }
        .onEach(::process)
        .catch { logger.error("Error while polling message bus.", it) }
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    private suspend fun process(message: Bundle) {
        val operationOutcome = validator.validate(message)
        val resources = message.resources<Resource>()

        if (operationOutcome.isAllOk()) {
            // MessageHeader's focus references should be versioned to simplify auditing.
            val header = resources.first() as MessageHeader
            header.focus.forEach { it.referenceElement = it.referenceElement.withVersion("1") }

            fhirRepo.addRange(resources)
        } else {
            val requestMessageHeader = resources.first() as MessageHeader
            val validationErrorResponse = createResponseMessage(requestMessageHeader, operationOutcome)
            messageBus.publish(validationErrorResponse)
        }
    }
}

private fun Bundle.hasDestination(expectedDestination: String): Boolean {
    val header = entry[0].resource as MessageHeader
    return header.destination?.count() == 1 && header.destination.any { it.endpoint == expectedDestination }
}

private fun createResponseMessage(
    requestMessageHeader: MessageHeader,
    operationOutcome: OperationOutcome
): Bundle {
    val outcomeCopy = operationOutcome.copy().apply {
        id = IdentityGenerator.createUUID5(requestMessageHeader.idAsUUID(), "details").toString()
    }

    val responseMessageHeader = MessageHeader().apply {
        id = IdentityGenerator.createUUID5(requestMessageHeader.idAsUUID(), "response").toString()
        event = requestMessageHeader.event
        destination = listOf(asDestination(requestMessageHeader.source))
        source = asSource(requestMessageHeader.destination.single())
        response = MessageHeader.MessageHeaderResponseComponent().apply {
            identifier = requestMessageHeader.idElement.idPart
            code = MessageHeader.ResponseType.FATALERROR
            details = Reference(outcomeCopy)
        }
    }

    return Bundle().apply {
        id = UUID.randomUUID().toString()
        timestampElement = InstantType.withCurrentTime()
        type = Bundle.BundleType.MESSAGE
        addResource(responseMessageHeader, outcomeCopy)
    }
}

private fun asDestination(src: MessageHeader.MessageSourceComponent) =
    MessageHeader.MessageDestinationComponent(src.endpointElement).apply { name = src.name }

private fun asSource(dest: MessageHeader.MessageDestinationComponent) =
    MessageHeader.MessageSourceComponent(dest.endpointElement).apply { name = dest.name }
