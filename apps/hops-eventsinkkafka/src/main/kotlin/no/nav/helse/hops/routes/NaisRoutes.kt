package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.domain.EventSinkJob
import org.koin.ktor.ext.getKoin

fun Routing.naisRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    val koin = getKoin()

    get("/isReady") {
        try {
            checkNotNull(koin.get<EventSinkJob>())
            call.respondText("EventSinkKafka")
        } catch (ex: Throwable) {
            call.application.environment.log.warn("/isReady error.", ex)
            call.respond(HttpStatusCode.InternalServerError, ex.message ?: "No exception message.")
        }
    }
    get("/isAlive") {
        call.respondText("EventSinkKafka")
    }
    get("/prometheus") {
        call.respond(prometheusMeterRegistry.scrape())
    }
}
