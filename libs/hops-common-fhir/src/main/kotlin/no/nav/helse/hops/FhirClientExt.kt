package no.nav.helse.hops

import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

inline fun <reified T : Resource> IGenericClient.allByQuery(query: String): Sequence<T> =
    allByUrl("${T::class.java.simpleName}?$query").mapNotNull { it as T }

/** Returns a Sequence of results where pagination is automatically handled during iteration. **/
fun IGenericClient.allByUrl(url: String): Sequence<Resource> =
    sequence {
        var bundle: Bundle? = this@allByUrl
            .search<Bundle>()
            .byUrl(url)
            .execute()

        while (bundle?.entry?.isEmpty() == false) {
            yieldAll(bundle.resources())
            bundle = nextPageOrNull(bundle)
        }
    }

private fun IGenericClient.nextPageOrNull(bundle: Bundle): Bundle? =
    if (bundle.link?.any { it.relation == Bundle.LINK_NEXT } == true)
        loadPage().next(bundle).execute()
    else
        null
