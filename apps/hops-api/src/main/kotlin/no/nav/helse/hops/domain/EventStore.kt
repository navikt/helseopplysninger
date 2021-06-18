package no.nav.helse.hops.domain

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

interface EventStore {
    suspend fun search(query: String, accept: ContentType, requestId: String): HttpResponse
    suspend fun publish(body: ByteReadChannel, contentType: ContentType, requestId: String): HttpResponse
}
