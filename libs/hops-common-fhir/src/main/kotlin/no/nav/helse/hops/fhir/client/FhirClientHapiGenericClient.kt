package no.nav.helse.hops.fhir.client

import ca.uhn.fhir.rest.client.api.IGenericClient
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.toUriType
import no.nav.helse.hops.fhir.weakEtag
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DomainResource
import java.util.UUID
import kotlin.reflect.KClass

class FhirClientHapiGenericClient(private val hapiClient: IGenericClient) : FhirClient {
    override suspend fun <T : DomainResource> read(type: KClass<T>, id: UUID) =
        hapiClient.read().resource(type.java).withId(id.toString()).execute()

    override suspend fun <T : DomainResource> vread(
        type: KClass<T>,
        id: UUID,
        version: Int
    ) = hapiClient.read().resource(type.java).withIdAndVersion(id.toString(), version.toString()).execute()

    override suspend fun <T : DomainResource> history(
        type: KClass<T>,
        id: UUID,
        query: String
    ) =
        hapiClient.allByUrl("${type.java.simpleName}/$id/_history?$query")

    override suspend fun <T : DomainResource> search(type: KClass<T>, query: String) =
        hapiClient.allByUrl("${type.java.simpleName}?$query")

    override suspend fun upsertAsTransaction(resources: List<DomainResource>): List<DomainResource> {
        if (resources.isEmpty()) return emptyList()
        val transaction = createTransaction(resources)

        val result = hapiClient
            .transaction()
            .withBundle(transaction)
            .encodedJson()
            .execute()

        check(result.link.none { it.relation == Bundle.LINK_NEXT }) {
            "Unexpected 'next' link in Transaction result." }

        return result.resources()
    }
}

/** Returns a Sequence of results where pagination is automatically handled during iteration. **/
private fun IGenericClient.allByUrl(relativeUrl: String): Sequence<DomainResource> =
    sequence {
        var bundle = this@allByUrl
            .search<Bundle>()
            .byUrl(relativeUrl)
            .execute()

        while (true) {
            yieldAll(bundle.resources())
            bundle = nextPageOrNull(bundle) ?: break
        }
    }

private fun IGenericClient.nextPageOrNull(bundle: Bundle) =
    if (bundle.link?.any { it.relation == Bundle.LINK_NEXT } == true)
        loadPage().next(bundle).execute()
    else
        null

private fun createTransaction(resources: List<DomainResource>) =
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
