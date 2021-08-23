package fileshare.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {

    get("/isReady") {
        try {
            call.respondText("FileShare")
        } catch (ex: Throwable) {
            call.application.environment.log.warn("/isReady error.", ex)
            call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
        }
    }
    get("/isAlive") {
        call.respondText("FileShare")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
