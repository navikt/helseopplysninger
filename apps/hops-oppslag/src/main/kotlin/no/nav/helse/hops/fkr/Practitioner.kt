package no.nav.helse.hops.fkr

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getPractitioner() {
    get("/Practitioner/") {
        call.respondText("oppslag")
    }
}