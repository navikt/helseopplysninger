package eventreplay.domain

import java.time.LocalDateTime
import java.util.UUID

class FhirMessage(
    val id: UUID,
    val timestamp: LocalDateTime,
    val content: ByteArray,
    val contentType: String,
    val sourceOffset: Long
) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.startsWith("application/fhir+json; fhirVersion=")) { "Unexpected Content-Type format." }
        require(sourceOffset >= 0) { "Source-Offset cannot be negative." }
    }
}
