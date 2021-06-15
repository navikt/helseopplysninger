package no.nav.helse.hops.domain

interface FhirMessageBus {
    suspend fun publish(message: FhirMessage)
}
