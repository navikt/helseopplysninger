package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.domain.GenericMessage
import org.koin.ktor.ext.inject
import java.net.URI
import java.time.LocalDateTime

fun Routing.fhirRoutes() {
    val searchService: FhirMessageSearchService by inject()
    val processService: FhirMessageProcessService by inject()

    get("/Bundle") {
        val base = URI(call.request.path().substringBeforeLast('/'))
        val lastUpdatedParam = call.request.queryParameters["_lastUpdated"]
        val rcvParam = call.request.queryParameters["message.destination-uri"]

        val since =
            if (lastUpdatedParam == null) LocalDateTime.MIN
            else LocalDateTime.parse(lastUpdatedParam.substringAfter("gt"))

        val rcv = if (rcvParam != null) URI(rcvParam) else null

        val searchResult = searchService.search(base, since, rcv)
        call.respond(searchResult)
    }

    post("/\$process-message") {
        val inputMessage = GenericMessage(call.receive())
        val processResult = processService.process(inputMessage)
        call.respond(processResult.bundle)
    }
}
