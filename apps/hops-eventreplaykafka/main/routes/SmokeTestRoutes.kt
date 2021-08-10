package routes

import domain.EventStore
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.koin.ktor.ext.inject

fun Routing.smokeTestRoutes() {
    route("/smokeTests") {
        val eventStore by inject<EventStore>()

        get("/eventStore") {
            try {
                eventStore.search(0).take(1).toList()
                call.respondText { "OK!" }
            } catch (ex: Throwable) {
                call.application.environment.log.warn("/smokeTests/eventStore error.", ex)
                call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
            }
        }
    }
}
