package no.nav.helse.hops.fhir.client

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import kotlinx.coroutines.flow.map
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.UUID

suspend inline fun <reified T : Resource> FhirClientReadOnly.read(id: UUID) =
    read(ResourceType.fromCode(T::class.simpleName), id) as T

suspend inline fun <reified T : Resource> FhirClientReadOnly.readOrNull(id: UUID) =
    try { read<T>(id) } catch (ex: ResourceNotFoundException) { null }

suspend inline fun <reified T : Resource> FhirClientReadOnly.vread(id: UUID, version: Int) =
    vread(ResourceType.fromCode(T::class.simpleName), id, version) as T

inline fun <reified T : Resource> FhirClientReadOnly.history(id: UUID, query: String = "") =
    history(ResourceType.fromCode(T::class.simpleName), id, query).map { it as T }

inline fun <reified T : Resource> FhirClientReadOnly.search(query: String = "") =
    search(ResourceType.fromCode(T::class.simpleName), query).map { it as T }

suspend inline fun <reified T : Resource> FhirClient.add(resource: T): T {
    val defensiveCopy = resource.copy().apply { id = UUID.randomUUID().toString() } as T
    return upsert(defensiveCopy) as T
}

suspend inline fun <reified T : Resource> FhirClient.update(resource: T): T {
    require(resource.meta.versionId.toInt() > 0) { "Update requires a versioned resource." }
    return upsert(resource) as T
}
