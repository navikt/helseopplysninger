package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
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
                call.respond(HttpStatusCode.OK, "OK!")
            } catch (ex: Throwable) {
                call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
            }
        }
    }
}
