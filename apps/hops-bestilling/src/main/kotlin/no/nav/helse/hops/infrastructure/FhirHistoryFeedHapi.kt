package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import no.nav.helse.hops.domain.FhirHistoryFeed
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import kotlin.coroutines.coroutineContext

class FhirHistoryFeedHapi(
    private val fhirClient: IGenericClient
) : FhirHistoryFeed {
    override fun poll(): Flow<Resource> =
        flow {
            while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                var bundle = fhirClient
                    .history()
                    .onServer()
                    .returnBundle(Bundle::class.java)
                    .encodedJson()
//                    .since(Date())
                    .execute()

                while (coroutineContext.isActive && bundle?.entry?.isEmpty() == false) {
                    bundle.entry.forEach { emit(it.resource) }
                    bundle = nextPageOrNull(bundle)
                }

                kotlinx.coroutines.delay(5000)
            }
        }

    private fun nextPageOrNull(bundle: Bundle): Bundle? =
        if (bundle.link?.any { it.relation == "next" } == true)
            fhirClient.loadPage().next(bundle).execute()
        else
            null
}
