package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.OperationOutcome

fun OperationOutcome.isAllOk(): Boolean {
    val errorStates = listOf(
        OperationOutcome.IssueSeverity.FATAL,
        OperationOutcome.IssueSeverity.ERROR,
        OperationOutcome.IssueSeverity.WARNING
    )

    return issue.none { it.severity in errorStates }
}
