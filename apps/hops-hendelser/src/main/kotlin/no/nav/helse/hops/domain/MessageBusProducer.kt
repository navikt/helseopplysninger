package no.nav.helse.hops.domain

interface MessageBusProducer<T> {
    suspend fun publish(message: T)
}
