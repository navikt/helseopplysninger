package no.nav.helse.hops.domain

interface FhirMessageBus {
    suspend fun publish(message: FhirMessage)
    suspend fun sourceOffsetOfLatestMessage(): Long
}
