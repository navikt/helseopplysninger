package eventreplay.domain

interface FhirMessageBus {
    suspend fun publish(message: FhirMessage)
    suspend fun sourceOffsetOfLatestMessage(): Long
}
