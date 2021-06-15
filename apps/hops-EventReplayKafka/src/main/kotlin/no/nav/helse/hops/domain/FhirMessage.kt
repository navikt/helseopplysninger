package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle

data class FhirMessage(val content: Bundle, val contentType: String, val requestId: String, val sourceOffset: Long) {
    init {
        require(content.type == Bundle.BundleType.MESSAGE) { "Content must be a Message." }
        require(contentType.startsWith("application/fhir+json; fhirVersion=4.0"))
        require(requestId.isNotBlank()) { "Request-ID cannot be blank." }
    }
}
