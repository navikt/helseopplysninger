package no.nav.helse.hops.domain

interface EventStore {
    suspend fun add(event: FhirMessage)
    suspend fun smokeTest()
}
