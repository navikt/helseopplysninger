package no.nav.helse.hops.oppslag.fkr

import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Practitioner

interface FkrFacade {
    suspend fun practitionerName(hprNr: Int): String
}

class FkrFacadeImpl(private val _fhirClient: IGenericClient) : FkrFacade {
    override suspend fun practitionerName(hprNr: Int): String {

        val bundle = _fhirClient
            .search<Bundle>()
            .byUrl("Practitioner?identifier=urn:oid:2.16.578.1.12.4.1.4.4|$hprNr")
            .execute()

        val practitioner = bundle.entry.firstOrNull()?.resource as Practitioner?
        val name = practitioner?.name?.firstOrNull()?.family ?: ""

        return name
    }
}
