package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.pullResourceGraphSnapshot
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toIsoString
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.UriType
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

class FhirMessageSearchService(private val fhirClient: FhirClientReadOnly) {
    suspend fun search(base: URI, since: LocalDateTime = LocalDateTime.MIN, destination: URI? = null): Bundle {
        val query = createQuery(since, destination)
        val messages = fhirClient
            .search<MessageHeader>(query)
            .map { header ->
                val resources = fhirClient.pullResourceGraphSnapshot(header)
                createMessage(base, header, resources.drop(1))
            }
            .toList()

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            entry = messages.map { it.bundle }.asEntriesWithFullyQualifiedUrls(base)
        }
    }
}

private fun createQuery(since: LocalDateTime, dest: URI? = null): String {
    var query = "_sort=_lastUpdated" // ascending.
    if (since > LocalDateTime.MIN)
        query += "&_lastUpdated=gt${since.toIsoString()}"
    if (dest != null)
        query += "&destination-uri=$dest"

    return query
}

private fun createMessage(serverBase: URI, header: MessageHeader, data: List<Resource>) =
    GenericMessage(
        Bundle().apply {
            id = IdentityGenerator.createUUID5(header.idAsUUID(), header.meta.versionId).toString()
            timestampElement = InstantType(header.meta.lastUpdated)
            type = Bundle.BundleType.MESSAGE
            entry = listOf(header).plus(data).asEntriesWithFullyQualifiedUrls(serverBase)
        }
    )

private fun List<Resource>.asEntriesWithFullyQualifiedUrls(serverBase: URI) =
    map {
        Bundle.BundleEntryComponent().apply {
            fullUrlElement = UriType(IdType(serverBase.toString(), it.idElement.resourceType, it.idElement.idPart).value)
            resource = it
        }
    }
