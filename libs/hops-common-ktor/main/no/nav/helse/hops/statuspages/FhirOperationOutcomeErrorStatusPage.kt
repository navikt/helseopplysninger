package no.nav.helse.hops.statuspages

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.ApplicationResponse
import io.ktor.response.header
import io.ktor.response.respond
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.OperationOutcome
import java.util.Date

/** Represents exceptions as a FHIR OperationOutcome resource according to spec:
 * https://www.hl7.org/fhir/http.html#Status-Codes **/
fun StatusPages.Configuration.useFhirErrorStatusPage() =
    apply {
        exception<Throwable> { ex ->
            val outcome = createOperationOutcome(ex)
            if (ex is BaseServerResponseException) call.response.addHeaders(ex)

            call.respond(ex.statusCode(), outcome)

            throw ex
        }
    }

private fun createOperationOutcome(ex: Throwable) =
    if (ex is BaseServerResponseException && ex.operationOutcome != null) ex.operationOutcome
    else OperationOutcome().apply {
        meta = Meta().apply { lastUpdated = Date() }

        val issue = OperationOutcome.OperationOutcomeIssueComponent().apply {
            severity = OperationOutcome.IssueSeverity.ERROR
            code = OperationOutcome.IssueType.EXCEPTION
            diagnostics = ex.message
        }

        addIssue(issue)
    }

private fun ApplicationResponse.addHeaders(ex: BaseServerResponseException) {
    if (ex.hasResponseHeaders()) {
        ex.responseHeaders.forEach {
            it.value.forEach { value ->
                header(it.key, value)
            }
        }
    }
}

private fun Throwable.statusCode() =
    if (this is BaseServerResponseException) HttpStatusCode.fromValue(statusCode)
    else HttpStatusCode.InternalServerError
