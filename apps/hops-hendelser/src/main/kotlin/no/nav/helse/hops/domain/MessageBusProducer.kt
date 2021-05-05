package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle

interface MessageBusProducer {
    suspend fun publish(message: Bundle)
}
