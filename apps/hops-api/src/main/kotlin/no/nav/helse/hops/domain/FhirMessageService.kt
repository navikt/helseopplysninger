package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.fhir.addResource
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.read
import no.nav.helse.hops.fhir.client.readOrNull
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.fhir.resourcesWithResolvedReferences
import no.nav.helse.hops.toIsoString
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

class FhirMessageService(private val fhirClient: FhirClient) {

    suspend fun search(since: LocalDateTime = LocalDateTime.MIN, destination: URI? = null): Bundle {
        val query = createQuery(since, destination)
        val messages = fhirClient
            .search<MessageHeader>(query)
            .map { header ->
                val focusResources = fhirClient
                    .search(ResourceType.MessageHeader, "_id=${header.idAsUUID()}&_include=MessageHeader:focus")
                    .filter { it.id != header.id }
                    .toList()
                createMessage(header, focusResources)
            }
            .toList()

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            messages.forEach { addResource(it.bundle) }
        }
    }

    suspend fun process(message: GenericMessage): OkResponseMessage {
        val requestId = message.header.idAsUUID()
        var header = fhirClient.readOrNull<MessageHeader>(requestId)

        if (header == null) {
            val resources = message.bundle.resourcesWithResolvedReferences()
            fhirClient.upsertAsTransaction(resources)
            header = fhirClient.read(requestId)
        }

        val responseId = IdentityGenerator.createUUID5(requestId, "response")
        return OkResponseMessage(header, responseId)
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

private fun createMessage(header: MessageHeader, data: List<Resource>) =
    GenericMessage(Bundle().apply {
        id = IdentityGenerator.createUUID5(header.idAsUUID(), header.meta.versionId).toString()
        timestampElement = InstantType(header.meta.lastUpdated)
        type = Bundle.BundleType.MESSAGE
        addResource(header)
        data.forEach { addResource(it) }
    })
