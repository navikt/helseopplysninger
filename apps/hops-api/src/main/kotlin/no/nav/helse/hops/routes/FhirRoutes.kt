package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.helse.hops.domain.FhirMessageService
import no.nav.helse.hops.domain.GenericMessage
import org.hl7.fhir.r4.model.Bundle
import org.koin.ktor.ext.inject

fun Routing.fhirRoutes() {
    val messageService: FhirMessageService by inject()

    get("/Bundle") {
        val since = call.request.queryParameters["_lastUpdated"]
        val rcv = call.request.queryParameters["message.destination-uri"]
        val searchResult = messageService.search()
        call.respond(searchResult)
    }

    post("/\$process-message") {
        val inputMessage = GenericMessage(call.receive())
        val processResult = messageService.process(inputMessage)
        call.respond(processResult.bundle)
    }
}
