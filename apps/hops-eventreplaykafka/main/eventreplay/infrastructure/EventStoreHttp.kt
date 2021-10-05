package eventreplay.infrastructure

import eventreplay.domain.EventStore
import eventreplay.domain.FhirMessage
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.toJsonByteArray
import no.nav.helse.hops.security.toLocalDateTime
import org.hl7.fhir.r4.model.Bundle
import java.util.UUID

class EventStoreHttp(
    private val config: Config.EventStore,
    private val client: HttpClient
) : EventStore {
    override fun search(startingOffset: Long): Flow<FhirMessage> =
        flow {
            var offset = startingOffset
            var url: String? = "${config.baseUrl}/fhir/4.0/Bundle?_offset=$startingOffset"
            var httpTask = client.fhirGetAsync(url!!)

            do {
                val httpResponse = httpTask.await()
                val body: String = httpResponse.receive()
                val result = JsonConverter.parse<Bundle>(body)

                url = result.link?.singleOrNull { it.relation == Bundle.LINK_NEXT }?.url
                if (url != null) httpTask = client.fhirGetAsync(url) // fetch next page while processing current.

                fun toFhirMessage(entry: Bundle.BundleEntryComponent): FhirMessage {
                    val bundle = entry.resource as Bundle
                    return FhirMessage(
                        id = bundle.entry[0].resource.idAsUUID(),
                        timestamp = bundle.timestamp.toLocalDateTime(),
                        content = bundle.toJsonByteArray(),
                        contentType = httpResponse.contentType().toString(),
                        sourceOffset = offset++
                    )
                }

                if (result.hasEntry()) result.entry.map(::toFhirMessage).forEach { emit(it) }
            } while (url != null)
        }
}

private fun HttpClient.fhirGetAsync(url: String) =
    async {
        get<HttpResponse>(url) {
            accept(ContentTypes.fhirJsonR4)
            headers {
                append(HttpHeaders.XRequestId, UUID.randomUUID().toString())
            }
        }
    }
