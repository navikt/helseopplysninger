package no.nav.helse.hops.domain

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toLocalDateTime
import no.nav.helse.hops.toUri
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.util.UUID

class FhirMessageProcessService(private val eventStore: EventStoreRepository) {
    suspend fun process(message: Bundle, correlationId: String) {
        validate(message)
        val event = createEventDto(message, correlationId)
        eventStore.add(event)
    }

    private fun createEventDto(message: Bundle, correlationId: String): EventDto {
        val header = message.entry[0].resource as MessageHeader

        return EventDto(
            bundleId = message.idAsUUID(),
            messageId = header.idAsUUID(),
            correlationId = correlationId,
            eventType = createEventType(header),
            bundleTimestamp = message.timestamp.toLocalDateTime(),
            recorded = LocalDateTime.now(),
            source = header.source.endpoint,
            destinations = header.destination.map { it.endpoint }.filter { it.isNotBlank() },
            data = createJsonByteArray(message),
            dataType = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()
        )
    }

    /** Validerer melding ihht. https://www.hl7.org/fhir/messaging.html
     * kan erstattes egenlagde Bundle og\eller MessageHeader profiler istedenfor å gjøres her. **/
    private suspend fun validate(message: Bundle) {
        check(message.type == Bundle.BundleType.MESSAGE) { "Bundle must be of type 'Message'" }

        val header = message.entry.first().resource as MessageHeader
        val bundleId = message.idAsUUID()
        val headerId = header.idAsUUID()

        check(bundleId.variant() != 0) { "Bundle.Id must be valid UUID." }
        check(headerId.variant() != 0) { "MessageHeader.Id must be valid UUID." }
        check(bundleId != headerId) { "Bundle.id and MessageHeader.id cannot be equal." }
        checkNotNull(message.timestamp) { "Bundle.timestamp is required." }

        check(message.entry.first().fullUrl == headerId.toUri().toString()) {
            "entry.fullUrl does not match MessageHeader.id."
        }

        // Ensure that a response references a request that actually exists.
        // https://www.hl7.org/fhir/messaging.html#3.4.1.4
        header.response?.identifier?.let {
            val requestMessageId = UUID.fromString(it)
            eventStore.ensureExists(requestMessageId)
        }
    }
}

private fun createEventType(header: MessageHeader): String =
    if (header.hasEventUriType()) header.eventUriType.value
    else header.eventCoding.run {
        require(hasSystem() || hasCode()) { "Both 'System' and 'Code' cannot be empty." }
        var coding = "$system|$code"
        if (hasVersion()) coding += "|$version"
        return coding
    }

private fun createJsonByteArray(message: Bundle): ByteArray =
    ByteArrayOutputStream().use { stream ->
        OutputStreamWriter(stream).use { writer ->
            FhirContext
                .forCached(FhirVersionEnum.R4)
                .newJsonParser()
                .encodeResourceToWriter(message, writer)
        }

        return stream.toByteArray()
    }
