package eventsink.domain

import no.nav.helse.hops.plugin.FhirMessage

interface EventStore {
    suspend fun add(event: FhirMessage)
    suspend fun smokeTest()
}
