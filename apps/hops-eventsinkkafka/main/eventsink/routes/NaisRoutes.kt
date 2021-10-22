package eventsink.routes

import eventsink.domain.EventSinkJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    sinkJob: EventSinkJob,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    route("/actuator") {
        get("/ready") {
            val statusCode = if (sinkJob.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(statusCode, "EventSinkKafka")
        }
        get("/live") {
            call.respondText("EventSinkKafka")
        }
        get("/metrics") {
            call.respond(prometheusMeterRegistry.scrape())
        }
    }
}
