package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.api.Constants
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.pullResourceGraphSnapshot
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toIsoString
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.UriType
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID

class FhirMessageSearchService(private val fhirClient: FhirClientReadOnly) {
    suspend fun search(base: URL, since: LocalDateTime = LocalDateTime.MIN, destination: URI? = null): Bundle {
        val query = createMessageHeaderQuery(since, destination)
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

private fun createMessageHeaderQuery(since: LocalDateTime, dest: URI? = null): String {
    var query = "${Constants.PARAM_SORT}=${Constants.PARAM_LASTUPDATED}" // ascending.
    if (since > LocalDateTime.MIN)
        query += "&${Constants.PARAM_LASTUPDATED}=gt${since.toIsoString()}"
    if (dest != null)
        query += "&${MessageHeader.SP_DESTINATION_URI}=$dest"

    return query
}

private fun createMessage(serverBase: URL, header: MessageHeader, data: List<Resource>) =
    GenericMessage(
        Bundle().apply {
            id = IdentityGenerator.createUUID5(header.idAsUUID(), header.meta.versionId).toString()
            timestampElement = InstantType(header.meta.lastUpdated)
            type = Bundle.BundleType.MESSAGE
            entry = listOf(header).plus(data).asEntriesWithFullyQualifiedUrls(serverBase)
        }
    )

private fun List<Resource>.asEntriesWithFullyQualifiedUrls(serverBase: URL) =
    map {
        Bundle.BundleEntryComponent().apply {
            fullUrlElement = UriType("$serverBase/${it.fhirType()}/${it.idElement.idPart}")
            resource = it
        }
    }
