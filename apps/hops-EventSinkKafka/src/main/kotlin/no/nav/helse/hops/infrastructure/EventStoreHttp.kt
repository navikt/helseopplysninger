package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.FhirMessage

class EventStoreHttp(
    private val config: Configuration.EventStore,
    private val client: HttpClient
) : EventStore {
    override suspend fun add(event: FhirMessage) =
        client.post<Unit>("${config.baseUrl}/fhir/\$process-message") {
            body = event.content
            headers {
                append(HttpHeaders.ContentType, event.contentType)
                append(HttpHeaders.XRequestId, event.requestId)
            }
        }

    override suspend fun smokeTest() =
        client.get<Unit>("${config.baseUrl}/isAlive")
}
