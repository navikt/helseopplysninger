package questionnaire.infrastructure

import questionnaire.domain.EventStore
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
import java.net.URL

class EventStoreHttp(
    private val httpClient: HttpClient,
    private val config: Config.EventStore
) : EventStore {
    override suspend fun search(
        downstreamUrl: URL,
        accept: ContentType,
        requestId: String
    ): HttpResponse =
        httpClient.get("${config.baseUrl}/fhir/4.0/Bundle" + ifNotNull(downstreamUrl.query) { "?$it" }) {
            expectSuccess = false
            accept(accept)
            headers { appendUpstreamHeaders(downstreamUrl, requestId) }
        }

    override suspend fun publish(
        downstreamUrl: URL,
        body: ByteReadChannel,
        contentType: ContentType,
        requestId: String
    ): HttpResponse =
        httpClient.post("${config.baseUrl}/fhir/4.0/\$process-message") {
            this.body = body
            expectSuccess = false
            contentType(contentType)
            headers { appendUpstreamHeaders(downstreamUrl, requestId) }
        }
}

private inline fun <T> ifNotNull(src: T?, transform: (T) -> String) = src?.let(transform) ?: ""

private fun HeadersBuilder.appendUpstreamHeaders(downstreamUrl: URL, requestId: String) {
    append(HttpHeaders.XRequestId, requestId)
    append(HttpHeaders.XForwardedProto, downstreamUrl.protocol)
    append(HttpHeaders.XForwardedHost, "${downstreamUrl.host}:${downstreamUrl.port}")
}
