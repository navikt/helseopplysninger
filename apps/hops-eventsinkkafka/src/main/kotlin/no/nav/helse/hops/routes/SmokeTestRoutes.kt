package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import no.nav.helse.hops.domain.EventStore
import org.koin.ktor.ext.inject

fun Routing.smokeTestRoutes() {
    route("/smokeTests") {
        val eventStore by inject<EventStore>()

        get("/eventStore") {
            try {
                eventStore.smokeTest()
                call.respondText { "OK!" }
            } catch (ex: Throwable) {
                call.application.environment.log.warn("/smokeTests/eventStore error.", ex)
                call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
            }
        }
    }
}
