package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

fun Bundle.addResource(resource: Resource) {
    val entry = Bundle.BundleEntryComponent()
    entry.fullUrl = "urn:uuid:${resource.id}"
    entry.resource = resource
    addEntry(entry)
}
