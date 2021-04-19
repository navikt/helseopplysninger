package no.nav.helse.hops.domain

import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.OperationOutcome

interface FhirResourceValidator {
    suspend fun validate(resource: IBaseResource): OperationOutcome
}
