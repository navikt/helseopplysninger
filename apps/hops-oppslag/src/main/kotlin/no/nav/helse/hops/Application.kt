package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.hops.fkr.FkrKoinModule
import no.nav.helse.hops.fkr.getHello
import no.nav.helse.hops.fkr.getPractitioner
import org.koin.dsl.module
import org.koin.ktor.ext.Koin

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        modules(
            module { single { environment.config }},
            FkrKoinModule.instance)
    }
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
        getHello()
    }
}