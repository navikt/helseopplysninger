package no.nav.helse.hops.routes

import ca.uhn.fhir.rest.api.Constants
import io.ktor.application.call
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.domain.GenericMessage
import no.nav.helse.hops.routing.fullUrl
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.koin.ktor.ext.inject
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime

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
        val requestMsg = GenericMessage(call.receive())
        val responseMsg = processService.process(requestMsg)
        call.respond(responseMsg.bundle)
    }
}

private fun RequestConnectionPoint.fhirServerBase() =
    URL(fullUrl().toString().substringBefore('?').substringBeforeLast('/'))
