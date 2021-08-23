package eventstore.routes

import eventstore.domain.FhirMessageSearchService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    searchService: FhirMessageSearchService,
    meterRegistry: PrometheusMeterRegistry,
) {
    get("/isReady") {
        try {
            searchService.search(0, 0)
            call.respondText("EventStore")
        } catch (ex: Throwable) {
            call.application.environment.log.warn("/isReady error.", ex)
            call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
        }
    }
    get("/isAlive") {
        call.respondText("EventStore")
    }
    get("/prometheus") {
        call.respond(meterRegistry.scrape())
    }
}
