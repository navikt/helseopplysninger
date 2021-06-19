package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.FhirMessage
import java.util.UUID

class EventStoreHttp(
    private val config: Configuration.EventStore,
    private val client: HttpClient
) : EventStore {
    override suspend fun add(event: FhirMessage) =
        client.post<Unit>("${config.baseUrl}/fhir/4.0/\$process-message") {
            body = event.content
            headers {
                append(HttpHeaders.ContentType, event.contentType) // can be STU3, R4, R5 etc.
                append(HttpHeaders.XRequestId, UUID.randomUUID().toString())
            }
        }

    override suspend fun smokeTest() =
        client.get<Unit>("${config.baseUrl}/fhir/4.0/Bundle?_count=1") {
            accept(ContentTypes.fhirJsonR4)
            headers {
                append(HttpHeaders.XRequestId, UUID.randomUUID().toString())
            }
        }
}
