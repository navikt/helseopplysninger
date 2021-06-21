package no.nav.helse.hops.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun EventStore.poll(startingOffset: Long): Flow<FhirMessage> =
    flow {
        var offset = startingOffset

        while (true) {
            search(offset).collect { emit(it); offset = it.sourceOffset }
            delay(2000) // cancellable
        }
    }
