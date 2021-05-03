package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    get("/isReady") {
        call.respondText("api")
    }
    get("/isAlive") {
        call.respondText("api")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
    get("/naishello") {
        call.respond("Naishello")
    }
}
