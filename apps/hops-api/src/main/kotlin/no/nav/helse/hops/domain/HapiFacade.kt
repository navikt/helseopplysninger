package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import java.util.UUID
import kotlin.reflect.KClass

interface HapiFacade {
    suspend fun tasks(): List<Task>
    suspend fun upsert(res: IBaseResource): IBaseResource
    suspend fun <T : IBaseResource> read(type: KClass<T>, id: UUID): IBaseResource?
}

suspend inline fun <reified T : Resource> HapiFacade.add(res: T): T {
    res.id = UUID.randomUUID().toString()
    return upsert(res) as T
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

    override suspend fun upsert(res: IBaseResource): IBaseResource {
        UUID.fromString(res.idElement.idPart) // Throws if not a valid UUID.
        return _fhirClient.update().resource(res).execute().resource!!
    }

    override suspend fun <T : IBaseResource> read(type: KClass<T>, id: UUID): IBaseResource? =
        try {
            _fhirClient.read().resource(type.java).withId(id.toString()).execute()
        } catch (ex: ResourceNotFoundException) {
            null
        }
}
