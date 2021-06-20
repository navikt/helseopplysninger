package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import no.nav.helse.hops.domain.EventStore
import java.net.URL

class EventStoreHttp(
    private val httpClient: HttpClient,
    private val config: Configuration.EventStore
) : EventStore {
    override suspend fun search(downstreamUrl: URL, accept: ContentType, requestId: String) =
        httpClient.get<HttpResponse>("${config.baseUrl}/fhir/4.0/Bundle?${downstreamUrl.query}") {
            expectSuccess = false
            accept(accept)
            headers { appendUpstreamHeaders(downstreamUrl, requestId) }
        }

    override suspend fun publish(downstreamUrl: URL, body: ByteReadChannel, contentType: ContentType, requestId: String) =
        httpClient.post<HttpResponse>("${config.baseUrl}/fhir/4.0/\$process-message") {
            this.body = body
            expectSuccess = false
            contentType(contentType)
            headers { appendUpstreamHeaders(downstreamUrl, requestId) }
        }
}

private fun HeadersBuilder.appendUpstreamHeaders(downstreamUrl: URL, requestId: String) {
    append(HttpHeaders.XRequestId, requestId)
    append(HttpHeaders.XForwardedProto, downstreamUrl.protocol)
    append(HttpHeaders.XForwardedHost, "${downstreamUrl.host}:${downstreamUrl.port}")
}
