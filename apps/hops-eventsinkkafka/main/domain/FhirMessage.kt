package domain

class FhirMessage(val content: ByteArray, val contentType: String) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.isNotBlank()) { "ContentType cannot be blank." }
    }
}
