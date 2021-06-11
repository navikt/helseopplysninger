package no.nav.helse.hops.domain

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import no.nav.helse.hops.toUri
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID

class FhirMessageSearchService(private val eventStore: EventStoreReadOnlyRepository) {
    suspend fun search(base: URL, since: LocalDateTime = LocalDateTime.MIN, destination: URI? = null): Bundle {
        val events = eventStore.search(EventStoreReadOnlyRepository.Query())

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            entry = events.map(::toBundleEntry)
        }
    }
}

private fun toBundleEntry(event: EventDto): Bundle.BundleEntryComponent =
    ByteArrayInputStream(event.data).use {
        val parser = FhirContext
            .forCached(FhirVersionEnum.R4)
            .newJsonParser()
            .setOverrideResourceIdWithBundleEntryFullUrl(false)

        return Bundle.BundleEntryComponent().apply {
            fullUrl = event.bundleId.toUri().toString()
            resource = parser.parseResource(Bundle::class.java, it)
        }
    }
