package questionnaire.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.actuators(prometheusMeterRegistry: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/ready") { call.respond("ready") }
        get("/live") { call.respondText("live") }
        get("/metrics") { call.respond(prometheusMeterRegistry.scrape()) }
    }
}
