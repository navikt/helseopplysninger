package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.r4.model.Resource

interface FhirRepository {
    suspend fun addRange(resources: List<Resource>)
}

class FhirRepositoryImpl(private val fhirClient: IGenericClient) : FhirRepository {
    override suspend fun addRange(resources: List<Resource>) {
        val result = fhirClient
            .transaction()
            .withResources(resources)
            .encodedJson()
            .execute()
    }
}
