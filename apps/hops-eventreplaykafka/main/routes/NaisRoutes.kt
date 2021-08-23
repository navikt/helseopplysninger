package routes

import domain.EventReplayJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    replayJob: EventReplayJob,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    get("/isReady") {
        val statusCode = if (replayJob.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
        call.respond(statusCode, "EventReplayKafka")
    }
    get("/isAlive") {
        call.respondText("EventReplayKafka")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
