package no.nav.helse.hops.fhir.client

import ca.uhn.fhir.rest.api.Constants
import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.toUriType
import no.nav.helse.hops.fhir.weakEtag
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.UUID

class FhirClientHapi(private val hapiClient: IGenericClient) : FhirClient {
    override suspend fun read(type: ResourceType, id: UUID) =
        hapiClient.read().resource(type.name).withId(id.toString()).execute() as Resource

    override suspend fun vread(
        type: ResourceType,
        id: UUID,
        version: Int
    ) = hapiClient.read().resource(type.name).withIdAndVersion(id.toString(), version.toString()).execute() as Resource

    override fun history(
        type: ResourceType,
        id: UUID,
        query: String
    ) = hapiClient.allByUrl("${type.name}/$id/_history?$query")

    override fun search(type: ResourceType, query: String) =
        hapiClient.allByUrl("${type.name}?$query")

    override suspend fun upsert(resource: Resource): Resource {
        resource.idAsUUID() // throws IllegalArgumentException if not a valid UUID.

        return hapiClient
            .update()
            .resource(resource)
            .withAdditionalHeader(Constants.HEADER_IF_MATCH, resource.weakEtag())
            .encodedJson()
            .execute()
            .resource as Resource
    }

    override suspend fun upsertAsTransaction(resources: List<Resource>) {
        if (resources.isEmpty()) return
        val transaction = createTransaction(resources)

        hapiClient
            .transaction()
            .withBundle(transaction)
            .encodedJson()
            .execute()
    }
}

/** Returns a Sequence of results where pagination is automatically handled during iteration. **/
private fun IGenericClient.allByUrl(relativeUrl: String): Flow<Resource> =
    flow {
        var bundle = search<Bundle>().byUrl("$serverBase/$relativeUrl").execute()

        while (true) {
            bundle.resources<Resource>().forEach {
                if (!currentCoroutineContext().isActive) return@flow
                emit(it)
            }
            bundle = nextPageOrNull(bundle) ?: break
        }
    }

private fun IGenericClient.nextPageOrNull(bundle: Bundle) =
    if (bundle.link?.any { it.relation == Bundle.LINK_NEXT } == true)
        loadPage().next(bundle).execute()
    else
        null

private fun createTransaction(resources: List<Resource>) =
    Bundle().apply {
        type = Bundle.BundleType.TRANSACTION
        entry = resources.map {
            Bundle.BundleEntryComponent().apply {
                resource = it
                fullUrlElement = it.idElement.toUriType()
                request = Bundle.BundleEntryRequestComponent().apply {
                    method = Bundle.HTTPVerb.PUT
                    url = "${it.fhirType()}/${it.id}"
                    ifMatch = it.weakEtag()
                }
            }
        }
    }
