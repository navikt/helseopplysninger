package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.hops.fkr.FkrFacade
import no.nav.helse.hops.fkr.FkrKoinModule
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        modules(
            module { single { environment.config }}, // makes the configuration available to DI.
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

        val fkr: FkrFacade by inject()
        get("/behandler/{hprNr}") {
            call.parameters["hprNr"]?.toIntOrNull()?.let {
                val name = fkr.practitionerName(it)
                call.respondText(name)
            }
        }
    }
}