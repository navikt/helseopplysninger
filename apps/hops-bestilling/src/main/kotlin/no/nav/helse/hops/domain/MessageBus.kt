package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Bundle

interface MessageBus {
    suspend fun publish(message: Bundle)
    fun poll(): Flow<Bundle>
}
