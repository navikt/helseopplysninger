package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.OperationOutcome

class FhirException(
    val operationOutcome: OperationOutcome,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
