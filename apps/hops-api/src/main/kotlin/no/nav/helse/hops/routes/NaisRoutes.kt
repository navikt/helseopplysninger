package no.nav.helse.hops.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.naisRoutes() {
    get("/isReady") {
        call.respondText("api")
    }
    get("/isAlive") {
        call.respondText("api")
    }
}