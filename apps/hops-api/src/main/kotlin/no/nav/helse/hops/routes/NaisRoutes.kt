package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    get("/isReady") {
        call.respondText("API")
    }
    get("/isAlive") {
        call.respondText("API")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
