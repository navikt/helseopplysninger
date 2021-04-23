package no.nav.helse.hops.domain
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Task

interface HapiFacade {
    suspend fun tasks(): Array<Task>
}

class HapiFacadeImpl(private val _fhirClient: IGenericClient) : HapiFacade {
    override suspend fun tasks(): Array<Task> {

        val bundle = _fhirClient
            .search<Bundle>()
            .byUrl("Task")
            .execute()

        val task: Array<Task> = arrayOf(bundle.entry.firstOrNull()?.resource as Task)

        /* return bundle.entry as Array<Task> */
        return task
    }
}
