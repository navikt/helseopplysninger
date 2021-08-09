package domain

import kotlinx.coroutines.flow.firstOrNull
import no.nav.helse.hops.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.hops.fhir.client.search
import org.hl7.fhir.r4.model.Practitioner

interface FkrFacade {
    suspend fun practitionerName(hprNr: Int): String
}

class FkrFacadeImpl(private val fhirClient: FhirClientReadOnly) : FkrFacade {
    override suspend fun practitionerName(hprNr: Int): String {

        val practitioner = fhirClient
            .search<Practitioner>("identifier=urn:oid:2.16.578.1.12.4.1.4.4|$hprNr")
            .firstOrNull()

        return practitioner?.name?.firstOrNull()?.family ?: ""
    }
}
