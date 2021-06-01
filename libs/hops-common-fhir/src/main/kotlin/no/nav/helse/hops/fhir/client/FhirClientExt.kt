package no.nav.helse.hops.fhir.client

import org.hl7.fhir.r4.model.DomainResource
import java.util.UUID

suspend inline fun <reified T : DomainResource> FhirClientReadOnly.read(id: UUID) =
    read(T::class, id) as T
suspend inline fun <reified T : DomainResource> FhirClientReadOnly.vread(id: UUID, version: Int) =
    vread(T::class, id, version) as T
suspend inline fun <reified T : DomainResource> FhirClientReadOnly.history(id: UUID, query: String = "") =
    history(T::class, id, query).map { it as T }
suspend inline fun <reified T : DomainResource> FhirClientReadOnly.search(query: String = "") =
    search(T::class, query).map { it as T }

suspend inline fun <reified T : DomainResource> FhirClient.upsert(resource: T) =
    upsertAsTransaction(listOf(resource)).single() as T
