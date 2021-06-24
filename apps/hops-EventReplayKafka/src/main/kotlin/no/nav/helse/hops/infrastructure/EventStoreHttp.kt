package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.withCharset
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.toJsonByteArray
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.r4.model.Bundle
import java.util.UUID

class EventStoreHttp(
    private val config: Configuration.EventStore,
    private val client: HttpClient
) : EventStore {
    @FlowPreview
    override fun search(startingOffset: Long): Flow<FhirMessage> {
        var offset = startingOffset
        val ct = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()

        fun toFhirMessage(entry: Bundle.BundleEntryComponent): FhirMessage {
            val bundle = entry.resource as Bundle
            val id = bundle.entry[0].resource.idAsUUID()
            val ts = bundle.timestamp.toLocalDateTime()
            val content = bundle.toJsonByteArray()
            return FhirMessage(id, ts, content, ct, offset++)
        }

        return paginate(startingOffset)
            .filter { it.hasEntry() }
            .flatMapConcat { it.entry.map(::toFhirMessage).asFlow() }
    }

    private fun paginate(startingOffset: Long): Flow<Bundle> =
        flow {
            var url: String? = "${config.baseUrl}/fhir/4.0/Bundle?_offset=$startingOffset"

            do {
                val body = client.get<String>(url!!) {
                    accept(ContentTypes.fhirJsonR4)
                    headers {
                        append(HttpHeaders.XRequestId, UUID.randomUUID().toString())
                    }
                }

                val result = JsonConverter.parse<Bundle>(body)
                url = result.link?.singleOrNull { it.relation == Bundle.LINK_NEXT }?.url

                emit(result)
            } while (url != null)
        }
}
