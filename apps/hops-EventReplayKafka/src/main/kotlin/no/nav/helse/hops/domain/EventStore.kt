package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow

interface EventStore {
    fun search(startingOffset: Long): Flow<FhirMessage>
    suspend fun smokeTest()
}
