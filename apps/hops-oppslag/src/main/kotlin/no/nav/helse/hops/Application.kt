package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.hops.fkr.FkrFacade
import no.nav.helse.hops.fkr.FkrKoinModule
import no.nav.security.token.support.ktor.tokenValidationSupport
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
    install(Authentication) {
        tokenValidationSupport(config = environment.config)
    }
    routing {
        get("/isReady") {
            call.respondText("ready")
        }
        get("/isAlive") {
            call.respondText("alive")
        }

        authenticate {
            val fkr: FkrFacade by inject()
            get("/behandler/{hprNr}") {
                call.parameters["hprNr"]?.toIntOrNull()?.let {
                    val name = fkr.practitionerName(it)
                    call.respondText(name)
                }
            }
        }
    }
}