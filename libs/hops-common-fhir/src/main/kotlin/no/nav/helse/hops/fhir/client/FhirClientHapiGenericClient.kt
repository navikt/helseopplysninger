package no.nav.helse.hops.fhir.client

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.toUriType
import no.nav.helse.hops.fhir.weakEtag
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import java.util.UUID
import kotlin.reflect.KClass

class FhirClientHapiGenericClient(private val hapiClient: IGenericClient) : FhirClient {
    override suspend fun <T : Resource> read(type: KClass<T>, id: UUID) =
        hapiClient.read().resource(type.java).withId(id.toString()).execute()

    override suspend fun <T : Resource> vread(
        type: KClass<T>,
        id: UUID,
        version: Int
    ) = hapiClient.read().resource(type.java).withIdAndVersion(id.toString(), version.toString()).execute()

    override fun <T : Resource> history(
        type: KClass<T>,
        id: UUID,
        query: String
    ) = hapiClient.allByUrl("${type.java.simpleName}/$id/_history?$query")

    override fun <T : Resource> search(type: KClass<T>, query: String) =
        hapiClient.allByUrl("${type.java.simpleName}?$query")

    override suspend fun upsert(resource: Resource): Resource {
        UUID.fromString(resource.id) // throws IllegalArgumentException if not a valid UUID.
        return hapiClient
            .update()
            .resource(resource)
            .withAdditionalHeader("If-Match", resource.weakEtag())
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
        var bundle = this@allByUrl
            .search<Bundle>()
            .byUrl(relativeUrl)
            .execute()

        while (true) {
            bundle.resources<Resource>().forEach { emit(it) }
            if (!currentCoroutineContext().isActive) break
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
