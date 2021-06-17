package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
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
import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.requestId
import org.hl7.fhir.r4.model.Bundle
import java.util.UUID

class EventStoreHttp(
    private val config: Configuration.EventStore,
    private val client: HttpClient
) : EventStore {
    override fun search(startingOffset: Long): Flow<FhirMessage> =
        flow {
            var msgOffset = startingOffset
            var url: String? = "${config.baseUrl}/fhir/Bundle?_offset=$startingOffset"
            var httpTask = client.fhirGetAsync(url!!)
            val parser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()

            do {
                val httpResponse = httpTask.await()
                val result = JsonConverter.parse<Bundle>(httpResponse.receive())
                val contentType = httpResponse.contentType().toString()

                url = result.link?.singleOrNull { it.relation == Bundle.LINK_NEXT }?.url
                if (url != null) httpTask = client.fhirGetAsync(url) // fetch next page while processing current.

                fun toFhirMessage(entry: Bundle.BundleEntryComponent) =
                    FhirMessage(entry.resource.idAsUUID(), ByteArray(0), contentType, entry.requestId, ++msgOffset)

                if (result.hasEntry()) result.entry.map(::toFhirMessage).forEach { emit(it) }
            } while (url != null)
        }

    override suspend fun smokeTest() =
        client.get<Unit>("${config.baseUrl}/isAlive")
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
