package no.nav.helse.hops.domain

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toUri
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime

class FhirMessageProcessService(private val eventStore: EventStoreRepository) {
    suspend fun process(message: IBaseBundle, correlationId: String) {
        if (message is Bundle) {
            val event = createEventDto(message, correlationId)
            eventStore.add(event)
        } else {
            throw NotImplementedError("${message.javaClass.name} is not supported.")
        }
    }

    private fun createEventDto(message: Bundle, correlationId: String): EventDto {
        // validate(message)
        val header = message.entry[0].resource as MessageHeader

        return EventDto(
            bundleId = message.idAsUUID(),
            messageId = header.idAsUUID(),
            correlationId = correlationId,
            eventType = createEventType(header),
            recorded = LocalDateTime.now(),
            source = header.source.endpoint,
            destinations = header.destination.map { it.endpoint }.filter { it.isNotBlank() },
            data = createJsonByteArray(message),
            dataType = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()
        )
    }

    /** Validerer melding ihht. https://www.hl7.org/fhir/messaging.html **/
    private fun validate(message: Bundle) {
        check(message.type == Bundle.BundleType.MESSAGE) { "Bundle must be of type 'Message'" }

        val header = message.entry.firstOrNull()?.resource as? MessageHeader
        checkNotNull(header) { "First resource in Bundle must be MessageHeader." }

        val bundleId = message.idAsUUID()
        val headerId = header.idAsUUID()

        check(bundleId.variant() != 0) { "Bundle.Id must be valid UUID." }
        check(headerId.variant() != 0) { "MessageHeader.Id must be valid UUID." }
        check(bundleId != headerId) { "Bundle.id and MessageHeader.id cannot be equal." }
        checkNotNull(message.timestamp) { "Bundle.timestamp is required." }
        check(header.source.endpoint.isNullOrBlank()) { "MessageHeader.source.endpoint is required." }

        check(message.entry.first().fullUrl == headerId.toUri().toString()) {
            "entry.fullUrl does not match MessageHeader.id."
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
