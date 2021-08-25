package eventsink.routes

import eventsink.domain.EventSinkJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    sinkJob: EventSinkJob,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    get("/isReady") {
        val statusCode = if (sinkJob.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
        call.respond(statusCode, "EventSinkKafka")
    }
    get("/isAlive") {
        call.respondText("EventSinkKafka")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
