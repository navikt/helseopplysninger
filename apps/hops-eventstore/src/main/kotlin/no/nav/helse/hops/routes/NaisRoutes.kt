package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.domain.FhirMessageSearchService
import org.koin.ktor.ext.inject

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    val searchService: FhirMessageSearchService by inject()

    get("/isReady") {
        try {
            searchService.search(1, 0)
            call.respondText("EventStore")
        } catch (ex: Throwable) {
            call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
        }
    }
    get("/isAlive") {
        call.respondText("EventStore")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
