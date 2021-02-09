package no.nav.helse.hops.fkr

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.get

fun Route.getPractitioner() {

    val fkr = get<FkrFacade>()

    get("/Practitioner") {
        val practitioner = fkr.practitioner(123)
        call.respondText(practitioner)
    }
}

fun Route.getHello() {
    get("/Hello") {
        call.respondText("Hello! ${context.request.authorization()}")
    }
}