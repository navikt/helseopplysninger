package no.nav.helse.hops.fhir.client

import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.UUID

interface FhirClientReadOnly {
    suspend fun read(type: ResourceType, id: UUID): Resource
    suspend fun vread(type: ResourceType, id: UUID, version: Int): Resource
    fun history(type: ResourceType, id: UUID, query: String = ""): Flow<Resource>
    fun search(type: ResourceType, query: String = ""): Flow<Resource>
}

interface FhirClient : FhirClientReadOnly {
    suspend fun upsert(resource: Resource): Resource

    /* Atomically upserts all the resources. The id of all the resources must be a valid UUID. */
    suspend fun upsertAsTransaction(resources: List<Resource>)
}
