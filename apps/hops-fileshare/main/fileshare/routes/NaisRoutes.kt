package fileshare.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/ready") {
            try {
                call.respondText("FileShare")
            } catch (ex: Throwable) {
                call.application.environment.log.warn("/actuator/ready error.", ex)
                call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
            }
        }
        get("/live") {
            call.respondText("FileShare")
        }
        get("/metrics") {
            call.respond(prometheusMeterRegistry.scrape())
        }
    }
}
