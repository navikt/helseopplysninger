package no.nav.helse.hops

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

fun Bundle.addResource(resource: Resource) {
    val entry = Bundle.BundleEntryComponent()
    entry.resource = resource
    addEntry(entry)
}

fun IBaseResource.toJson(): String {
    val ctx = FhirContext.forCached(FhirVersionEnum.R4)!!
    val parser = ctx.newJsonParser()!!
    return parser.encodeResourceToString(this)
}
