package eventsink.routes

import eventsink.domain.EventStore
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.smokeTestRoutes(eventStore: EventStore) {
    route("/smokeTests") {
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
