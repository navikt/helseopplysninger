package no.nav.helse.hops.fhir.client

import org.hl7.fhir.r4.model.Resource
import java.util.UUID
import kotlin.reflect.KClass

interface FhirClientReadOnly {
    suspend fun <T : Resource> read(type: KClass<T>, id: UUID): Resource
    suspend fun <T : Resource> vread(type: KClass<T>, id: UUID, version: Int): Resource
    suspend fun <T : Resource> history(type: KClass<T>, id: UUID, query: String = ""): Sequence<Resource>
    suspend fun <T : Resource> search(type: KClass<T>, query: String = ""): Sequence<Resource>
}

interface FhirClient : FhirClientReadOnly {
    /* Atomically upserts all the resources. The id of all the resources must be a valid UUID. */
    suspend fun upsertAsTransaction(resources: List<Resource>): List<Resource>
}
