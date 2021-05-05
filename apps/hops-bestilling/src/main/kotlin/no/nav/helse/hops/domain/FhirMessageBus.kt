package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Bundle

interface MessageBusConsumer<T> {
    fun poll(): Flow<T>
}

interface MessageBusProducer<T> {
    suspend fun publish(message: T)
}

interface FhirMessageBus : MessageBusConsumer<Bundle>, MessageBusProducer<Bundle>
