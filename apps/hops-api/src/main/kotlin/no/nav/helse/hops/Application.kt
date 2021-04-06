package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.features.DefaultHeaders
import io.ktor.features.CallLogging
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.routing.get
import no.nav.helse.hops.auth.configureAuthentication
import no.nav.helse.hops.routes.naisRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    install(DefaultHeaders)
    install(CallLogging)
    configureAuthentication()
    routing {
        naisRoutes()
        authenticate {
            get("/") {
                call.respond(OK)
            }
        }
    }
}
