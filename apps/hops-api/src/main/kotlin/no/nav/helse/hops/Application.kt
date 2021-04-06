package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.features.DefaultHeaders
import io.ktor.features.CallLogging
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.routing.get
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.auth.configureAuthentication
import no.nav.helse.hops.routes.naisRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    install(DefaultHeaders)
    install(CallLogging)
    configureAuthentication()
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    routing {
        naisRoutes(prometheusMeterRegistry)
        authenticate {
            get("/") {
                call.respond(OK)
            }
        }
    }
}
