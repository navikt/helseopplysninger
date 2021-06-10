package no.nav.helse.hops.fhir.client

import org.hl7.fhir.r4.model.Resource
import java.util.UUID

suspend inline fun <reified T : Resource> FhirClient.add(resource: T): T {
    val defensiveCopy = resource.copy().apply { id = UUID.randomUUID().toString() }
    return upsert(defensiveCopy) as T
}

suspend inline fun <reified T : Resource> FhirClient.update(resource: T): T {
    require(resource.meta.versionId.toInt() > 0) { "Update requires a versioned resource." }
    return upsert(resource) as T
}
