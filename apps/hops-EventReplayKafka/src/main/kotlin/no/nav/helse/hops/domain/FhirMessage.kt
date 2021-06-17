package no.nav.helse.hops.domain

import java.time.LocalDateTime
import java.util.UUID

class FhirMessage(
    val id: UUID,
    val timestamp: LocalDateTime,
    val content: ByteArray,
    val contentType: String,
    val requestId: String,
    val sourceOffset: Long
) {
    init {
        require(timestamp.isBefore(LocalDateTime.now())) { "Timestamp cannot be in the future." }
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.startsWith("application/fhir+json; fhirVersion=")) { "Unexpected Content-Type format." }
        require(requestId.isNotBlank()) { "Request-ID cannot be blank." }
        require(sourceOffset >= 0) { "Source-Offset cannot be negative." }
    }
}
