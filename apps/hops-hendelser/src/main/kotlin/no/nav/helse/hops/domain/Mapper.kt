package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface Mapper<T, R> {
    suspend fun map(input: T): R
}

fun <T, R> Flow<T>.mapWith(mapper: Mapper<T, R>) = map { mapper.map(it) }
