package no.nav.helse.hops.hops.fhir.client

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import no.nav.helse.hops.hops.fhir.idAsUUID
import no.nav.helse.hops.hops.toIsoString
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.time.LocalDateTime
import java.util.UUID

suspend inline fun <reified T : Resource> FhirClientReadOnly.read(id: UUID) =
    read(ResourceType.fromCode(T::class.simpleName), id) as T

suspend inline fun <reified T : Resource> FhirClientReadOnly.readOrNull(id: UUID) =
    try { read<T>(id) } catch (ex: ResourceNotFoundException) { null }

suspend inline fun <reified T : Resource> FhirClientReadOnly.contains(resource: T) =
    readOrNull<T>(resource.idAsUUID()) != null

suspend inline fun <reified T : Resource> FhirClientReadOnly.vread(id: UUID, version: Int) =
    vread(ResourceType.fromCode(T::class.simpleName), id, version) as T

/** Returns the version of the resource at the supplied timestamp.
 * Throws NoSuchElementException if no applicable version exists. **/
suspend fun FhirClientReadOnly.readHistory(type: ResourceType, id: UUID, at: LocalDateTime) =
    history(type, id, "_at=le${at.toIsoString()}&_count=1").single()

/** Returns the version of the resource at the supplied timestamp.
 * Throws NoSuchElementException if no applicable version exists. **/
suspend inline fun <reified T : Resource> FhirClientReadOnly.readHistory(id: UUID, at: LocalDateTime) =
    readHistory(ResourceType.fromCode(T::class.simpleName), id, at) as T

inline fun <reified T : Resource> FhirClientReadOnly.history(id: UUID, query: String = "") =
    history(ResourceType.fromCode(T::class.simpleName), id, query).map { it as T }

inline fun <reified T : Resource> FhirClientReadOnly.search(query: String = "") =
    search(ResourceType.fromCode(T::class.simpleName), query).map { it as T }
