package no.nav.helse.hops.domain

import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.requestId
import no.nav.helse.hops.toUri
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.UUID

class FhirMessageSearchService(private val eventStore: EventStoreReadOnlyRepository) {
    suspend fun search(count: Int, offset: Long, destination: URI? = null): Bundle {
        val parser = JsonConverter.newParser()

        fun toBundleEntry(event: EventDto) =
            ByteArrayInputStream(event.data).use {
                Bundle.BundleEntryComponent().apply {
                    fullUrl = event.bundleId.toUri().toString()
                    resource = parser.parseResource(Bundle::class.java, it)
                    requestId = event.requestId
                }
            }

        val query = EventStoreReadOnlyRepository.Query(count, offset, destination?.toString())
        val events = eventStore.search(query)

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            entry = events.map(::toBundleEntry)
        }
    }
}
