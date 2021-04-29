package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow

interface MessageBusConsumer<T> {
    fun poll(): Flow<T>
}
