package no.nav.helse.hops.domain

import java.util.UUID

class FhirMessage(val id: UUID, val content: ByteArray, val contentType: String, val requestId: String, val sourceOffset: Long) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.startsWith("application/fhir+json; fhirVersion="))
        require(requestId.isNotBlank()) { "Request-ID cannot be blank." }
    }
}
