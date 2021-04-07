package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome

interface FhirMessageProcessor {
    suspend fun process(message: Bundle): OperationOutcome
}
