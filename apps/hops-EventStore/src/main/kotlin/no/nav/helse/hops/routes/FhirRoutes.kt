package no.nav.helse.hops.routes

import ca.uhn.fhir.rest.api.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.routing.fullUrl
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.koin.ktor.ext.inject
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

fun Routing.fhirRoutes() {
    val searchService: FhirMessageSearchService by inject()
    val processService: FhirMessageProcessService by inject()

    get("/Bundle") {
        val base = call.request.local.fhirServerBase()
        val lastUpdatedParam = call.request.queryParameters[Constants.PARAM_LASTUPDATED]
        val rcvParam = call.request.queryParameters["${Bundle.SP_MESSAGE}.${MessageHeader.SP_DESTINATION_URI}"]

        val since =
            if (lastUpdatedParam == null) LocalDateTime.MIN
            else OffsetDateTime.parse(lastUpdatedParam.substringAfter("gt")).toLocalDateTime()

        val rcv = if (rcvParam != null) URI(rcvParam) else null

        val searchResult = searchService.search(base, since, rcv)
        call.respond(searchResult)
    }

    /** Processes the message event synchronously according to
     * https://www.hl7.org/fhir/messageheader-operation-process-message.html **/
    post("/${Constants.EXTOP_PROCESS_MESSAGE}") {
        with(call.request.queryParameters) {
            check(get(Constants.PARAM_ASYNC).toBoolean()) { "'async=true' is required." }
            check(get(Constants.PARAM_RESPONSE_URL) == null) { "'response-url' is not supported." }
        }

        val message: Bundle = call.receive()
        val requestId = call.request.header(Constants.HEADER_REQUEST_ID) ?: UUID.randomUUID().toString()

        processService.process(message, requestId)

        call.response.header(Constants.HEADER_REQUEST_ID, requestId) // http://hl7.org/fhir/http.html#custom
        call.respond(HttpStatusCode.Accepted)
    }
}

private fun RequestConnectionPoint.fhirServerBase() =
    URL(fullUrl().toString().substringBefore('?').substringBeforeLast('/'))
