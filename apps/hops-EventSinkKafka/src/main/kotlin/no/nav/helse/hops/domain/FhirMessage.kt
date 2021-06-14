package no.nav.helse.hops.domain

class FhirMessage(val content: ByteArray, val contentType: String, val requestId: String) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.isNotBlank()) { "ContentType cannot be blank." }
        require(requestId.isNotBlank()) { "Request-ID cannot be blank." }
    }
}
