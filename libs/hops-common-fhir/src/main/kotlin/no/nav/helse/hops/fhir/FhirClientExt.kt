package no.nav.helse.hops.fhir

import ca.uhn.fhir.rest.client.api.IGenericClient
import no.nav.helse.hops.fhir.models.Transaction
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

fun IGenericClient.executeTransaction(t: Transaction): Bundle =
    transaction().withBundle(t.bundle).execute()

inline fun <reified T : Resource> IGenericClient.allByQuery(query: String): Sequence<T> =
    allByUrl("${T::class.java.simpleName}?$query").map { it as T }

/** Returns a Sequence of results where pagination is automatically handled during iteration. **/
fun IGenericClient.allByUrl(url: String): Sequence<Resource> =
    sequence {
        var bundle = this@allByUrl
            .search<Bundle>()
            .byUrl(if (url.startsWith("http")) url else "$serverBase/$url")
            .execute()

        while (true) {
            yieldAll(bundle.resources())
            bundle = nextPageOrNull(bundle) ?: break
        }
    }

private fun IGenericClient.nextPageOrNull(bundle: Bundle): Bundle? =
    if (bundle.link?.any { it.relation == Bundle.LINK_NEXT } == true)
        loadPage().next(bundle).execute()
    else
        null
