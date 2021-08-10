package routes

import domain.EventSinkJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.ktor.ext.inject

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    val job: EventSinkJob by inject()

    get("/isReady") {
        val statusCode = if (job.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
        call.respond(statusCode, "EventSinkKafka")
    }
    get("/isAlive") {
        call.respondText("EventSinkKafka")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
