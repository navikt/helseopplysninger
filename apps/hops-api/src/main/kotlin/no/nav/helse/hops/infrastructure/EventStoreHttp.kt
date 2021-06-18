package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import no.nav.helse.hops.domain.EventStore

class EventStoreHttp(
    private val httpClient: HttpClient,
    private val config: Configuration.EventStore
) : EventStore {
    override suspend fun search(query: String, accept: ContentType, requestId: String) =
        httpClient.get<HttpResponse>("${config.baseUrl}/fhir/Bundle?$query") {
            expectSuccess = false
            accept(accept)
            headers {
                append(HttpHeaders.XRequestId, requestId)
            }
        }

    override suspend fun publish(body: ByteReadChannel, contentType: ContentType, requestId: String) =
        httpClient.post<HttpResponse>("${config.baseUrl}/fhir/\$process-message") {
            this.body = body
            expectSuccess = false
            contentType(contentType)
            headers {
                append(HttpHeaders.XRequestId, requestId)
            }
        }
}
