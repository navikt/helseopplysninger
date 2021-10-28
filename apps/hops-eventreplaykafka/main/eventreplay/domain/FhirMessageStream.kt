package eventreplay.domain

interface FhirMessageStream {
    suspend fun publish(message: FhirMessage)
    suspend fun sourceOffsetOfLatestMessage(): Long
}
