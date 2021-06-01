package no.nav.helse.hops.fhir.client

import org.hl7.fhir.r4.model.DomainResource
import java.util.UUID
import kotlin.reflect.KClass

interface FhirClientReadOnly {
    suspend fun <T : DomainResource> read(type: KClass<T>, id: UUID): DomainResource
    suspend fun <T : DomainResource> vread(type: KClass<T>, id: UUID, version: Int): DomainResource
    suspend fun <T : DomainResource> history(type: KClass<T>, id: UUID, query: String = ""): Sequence<DomainResource>
    suspend fun <T : DomainResource> search(type: KClass<T>, query: String = ""): Sequence<DomainResource>
}

interface FhirClient : FhirClientReadOnly {
    /* Atomically upserts all the resources. The id of all the resources must be a valid UUID. */
    suspend fun upsertAsTransaction(resources: List<DomainResource>): List<DomainResource>
}
