package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle

interface MessageBus {
    suspend fun publish(message: Bundle)
    suspend fun poll(): List<Bundle>
}
