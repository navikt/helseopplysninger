package no.nav.helse.hops.domain

import no.nav.helse.hops.fhir.messages.BaseMessage

interface MessageBusProducer {
    suspend fun publish(message: BaseMessage)
}
