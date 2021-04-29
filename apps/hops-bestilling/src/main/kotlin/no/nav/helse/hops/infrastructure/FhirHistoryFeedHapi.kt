package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nav.helse.hops.domain.FhirHistoryFeed
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

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

                while (bundle?.entry?.isEmpty() == false) {
                    bundle.entry.forEach { emit(it.resource) }
                    bundle = fhirClient.loadPage().next(bundle).execute()
                    kotlinx.coroutines.delay(1)
                }

                kotlinx.coroutines.delay(5000)
            }
        }
}