package no.nav.helse.hops

import no.nav.helse.hops.fkr.getPractitioner
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.oppslag() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("oppslag")
        }
        get("/isReady") {
            call.respondText("oppslag")
        }
        get("/isAlive") {
            call.respondText("oppslag")
        }
        getPractitioner()
    }
}