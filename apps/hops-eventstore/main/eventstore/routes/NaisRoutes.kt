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
    get("/actuator/ready") {
        try {
            searchService.search(0, 0)
            call.respondText("EventStore")
        } catch (ex: Throwable) {
            call.application.environment.log.warn("/actuator/ready error.", ex)
            call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
        }
    }
    get("/actuator/live") {
        call.respondText("EventStore")
    }
    get("/metrics") {
        call.respond(meterRegistry.scrape())
    }
}
