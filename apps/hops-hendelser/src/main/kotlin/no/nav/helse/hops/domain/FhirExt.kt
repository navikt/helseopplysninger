package no.nav.helse.hops.domain

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Resource

fun Bundle.addResource(resource: Resource) {
    val entry = Bundle.BundleEntryComponent()
    entry.fullUrl = "urn:uuid:${resource.id}"
    entry.resource = resource
    addEntry(entry)
}

fun IBaseResource.toJson(): String {
    val ctx = FhirContext.forCached(FhirVersionEnum.R4)
    val parser = ctx.newJsonParser()
    return parser.encodeResourceToString(this)
}

fun OperationOutcome.isAllOk(): Boolean {
    val errorStates = listOf(
        OperationOutcome.IssueSeverity.FATAL,
        OperationOutcome.IssueSeverity.ERROR,
        OperationOutcome.IssueSeverity.WARNING
    )

    return issue.none { it.severity in errorStates }
}
