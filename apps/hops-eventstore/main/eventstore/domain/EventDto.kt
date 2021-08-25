package eventstore.domain

import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.fullyQualifiedEventType
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.toJsonByteArray
import no.nav.helse.hops.security.toLocalDateTime
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import java.time.LocalDateTime
import java.util.UUID

class EventDto(
    val bundleId: UUID,
    val messageId: UUID,
    val eventType: String,
    val bundleTimestamp: LocalDateTime,
    val recorded: LocalDateTime,
    val source: String,
    val destinations: List<String>,
    val data: ByteArray,
    val dataType: String
) {
    companion object {
        fun create(message: Bundle) =
            (message.entry[0].resource as MessageHeader).let { header ->
                EventDto(
                    bundleId = message.idAsUUID(),
                    messageId = header.idAsUUID(),
                    eventType = header.fullyQualifiedEventType,
                    bundleTimestamp = message.timestamp.toLocalDateTime(),
                    recorded = LocalDateTime.now(),
                    source = header.source.endpoint,
                    destinations = header.destination.map { it.endpoint }.filter { it.isNotBlank() },
                    data = message.toJsonByteArray(),
                    dataType = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()
                )
            }
    }
}
