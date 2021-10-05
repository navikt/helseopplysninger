package api.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    get("/actuator/ready") {
        call.respondText("API")
    }
    get("/actuator/live") {
        call.respondText("API")
    }
    get("/metrics") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
