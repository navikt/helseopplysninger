package no.nav.helse.hops.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
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
}