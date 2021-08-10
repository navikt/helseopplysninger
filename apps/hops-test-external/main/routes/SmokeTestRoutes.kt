package routes

import domain.EventStore
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.koin.ktor.ext.inject

fun Routing.smokeTestRoutes() {
    route("/smokeTests") {
        val eventStore by inject<EventStore>()

        get("/external-api") {
            try {
                val response = eventStore.search()
                call.respondText { "OK!" }
            } catch (ex: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ex.message ?: "No exception message."
                )
            }
        }
    }
}
