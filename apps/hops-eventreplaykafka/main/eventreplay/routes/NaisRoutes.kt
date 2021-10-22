package eventreplay.routes

import eventreplay.domain.EventReplayJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    replayJob: EventReplayJob,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    route("/actuator") {
        get("/ready") {
            val statusCode = if (replayJob.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(statusCode, "EventReplayKafka")
        }
        get("/live") {
            call.respondText("EventReplayKafka")
        }
        get("/metrics") {
            call.respond(prometheusMeterRegistry.scrape())
        }
    }
}
