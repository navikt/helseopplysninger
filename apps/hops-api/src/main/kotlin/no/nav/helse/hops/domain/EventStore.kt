package no.nav.helse.hops.domain

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import java.net.URL

interface EventStore {
    suspend fun search(downstreamUrl: URL, accept: ContentType, requestId: String): HttpResponse
    suspend fun publish(downstreamUrl: URL, body: ByteReadChannel, contentType: ContentType, requestId: String): HttpResponse
}
