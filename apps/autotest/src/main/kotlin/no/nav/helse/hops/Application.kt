package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import no.nav.helse.hops.routes.indexRoutes


@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        indexRoutes()
    }
}
