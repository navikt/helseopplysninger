package e2e

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val config = loadConfigsOrThrow<Config>("/application.yaml")

    install(MicrometerMetrics) { registry = prometheus }
    install(DefaultHeaders)
    install(CallLogging)

    routing {
        actuators(prometheus)
        e2eTrigger(config)
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/actuator/ready") { call.respondText("ready") }
    get("/actuator/live") { call.respondText("live") }
    get("/metrics") { call.respond(prometheus.scrape()) }
}

private fun Routing.e2eTrigger(config: Config) {
    install(ContentNegotiation) { json(Json { prettyPrint = true }) }

    get("/runTests") {
        val e2e = TestExecutor(config.api.hops)
        val results = e2e.runTests()
        call.respond(results)
    }
}
