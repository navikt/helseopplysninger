package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import no.nav.helse.hops.routes.indexRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        indexRoutes()
    }
}
