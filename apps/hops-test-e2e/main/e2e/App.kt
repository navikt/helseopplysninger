package e2e

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) { registry = prometheus }

    routing {
        actuators(prometheus)
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/isReady") { call.respond("ready") }
    get("/isAlive") { call.respond("live") }
    get("/prometheus") { call.respond(prometheus.scrape()) }
}
