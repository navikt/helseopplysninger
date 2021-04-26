package no.nav.helse.hops.domain
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Task

interface HapiFacade {
    suspend fun tasks(): List<Task>
}

class HapiFacadeImpl(private val _fhirClient: IGenericClient) : HapiFacade {
    override suspend fun tasks(): List<Task> {
        val bundle = _fhirClient
            .search<Bundle>()
            .byUrl("Task")
            .execute()

        val entries = bundle.entry ?: arrayListOf()
        return entries.mapNotNull { it.resource as? Task }
    }
}
