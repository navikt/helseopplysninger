package no.nav.helse.hops.fhir.client

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import org.hl7.fhir.r4.model.Resource
import java.util.UUID

suspend inline fun <reified T : Resource> FhirClientReadOnly.read(id: UUID) =
    read(T::class, id) as T

suspend inline fun <reified T : Resource> FhirClientReadOnly.readOrNull(id: UUID) =
    try { read<T>(id) } catch (ex: ResourceNotFoundException) { null }

suspend inline fun <reified T : Resource> FhirClientReadOnly.vread(id: UUID, version: Int) =
    vread(T::class, id, version) as T

suspend inline fun <reified T : Resource> FhirClientReadOnly.history(id: UUID, query: String = "") =
    history(T::class, id, query).map { it as T }

suspend inline fun <reified T : Resource> FhirClientReadOnly.search(query: String = "") =
    search(T::class, query).map { it as T }

suspend inline fun <reified T : Resource> FhirClient.add(resource: T): T {
    val defensiveCopy = resource.copy().apply { id = UUID.randomUUID().toString() } as T
    return upsert(defensiveCopy) as T
}

suspend inline fun <reified T : Resource> FhirClient.update(resource: T): T {
    require(resource.meta.versionId.toInt() > 0) { "Update requires a versioned resource." }
    return upsert(resource) as T
}
