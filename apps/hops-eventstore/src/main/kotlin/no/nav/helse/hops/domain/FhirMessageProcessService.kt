package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toUri
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import java.util.Date
import java.util.UUID

class FhirMessageProcessService(private val eventStore: EventStoreRepository) {
    suspend fun process(message: Bundle) {
        validate(message)

        val event = EventDto.create(message)

        eventStore.getByIdOrNull(event.messageId)?.let { existing ->
            if (event.data.contentEquals(existing.data)) return
            throw ResourceVersionConflictException("A Message with ID=${event.messageId} already exists.")
        }

        eventStore.add(event)
    }

    private suspend fun validate(message: Bundle) {
        fhirValidator.validateWithResult(message).run {
            if (!isSuccessful) throw UnprocessableEntityException(toString(), toOperationOutcome())
        }

        // Validerer melding ihht. https://www.hl7.org/fhir/messaging.html
        // kan erstattes egenlagde Bundle og\eller MessageHeader profiler slik at FhirValidator kan håndtere det.
        try {
            check(message.type == Bundle.BundleType.MESSAGE) { "Bundle must be of type 'Message'" }

            val header = message.entry.first().resource as MessageHeader
            val bundleId = message.idAsUUID()
            val headerId = header.idAsUUID()

            check(bundleId.variant() != 0) { "Bundle.Id must be valid UUID." }
            check(headerId.variant() != 0) { "MessageHeader.Id must be valid UUID." }
            check(bundleId != headerId) { "Bundle.id and MessageHeader.id cannot be equal." }
            checkNotNull(message.timestamp) { "Bundle.timestamp is required." }
            check(message.timestamp.before(Date())) { "Bundle.timestamp cannot be in the future." }

            check(message.entry.first().fullUrl == headerId.toUri().toString()) {
                "entry.fullUrl does not match MessageHeader.id."
            }

            // Ensure that a response references a request that actually exists.
            // https://www.hl7.org/fhir/messaging.html#3.4.1.4
            header.response?.identifier?.let {
                val requestMessageId = UUID.fromString(it)
                eventStore.ensureExists(requestMessageId)
            }
        } catch (ex: Throwable) {
            throw UnprocessableEntityException(ex.message, ex)
        }
    }
}

// The validator is thread-safe and can safely be re-used.
private val fhirValidator by lazy { FhirValidatorFactory.create() }
