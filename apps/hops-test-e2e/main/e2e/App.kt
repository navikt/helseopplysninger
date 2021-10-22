package e2e

import e2e._common.E2eExecutor
import e2e._common.e2eExecutor
import e2e.api.apiTests
import e2e.replay.replayTests
import e2e.sink.sinkTests
import e2e.store.storeTests
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
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.slf4j.MDC
import java.util.UUID
import kotlin.time.ExperimentalTime

private val log = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(ContentNegotiation) { json(Json { prettyPrint = true }) }
    install(MicrometerMetrics) { registry = prometheus }
    install(DefaultHeaders)
    install(CallLogging) { logger = log }

    val e2e = e2eExecutor {
        add(apiTests())
        add(replayTests())
        add(sinkTests())
        add(storeTests())
    }

    routing {
        trigger(e2e)
        actuators(prometheus)
    }
}

@OptIn(ExperimentalTime::class)
private fun Routing.trigger(e2e: E2eExecutor) {
    get("/runTests") {
        MDC.put("checksum", UUID.randomUUID().toString())
        log.info("Running all tests...")
        val result = e2e.exec()
        log.info("${e2e.size} tests completed in ${result.totalDurationMs}")
        MDC.remove("checksum")
        call.respond(result)
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/ready") { call.respondText("ready") }
        get("/live") { call.respondText("live") }
        get("/metrics") { call.respond(prometheus.scrape()) }
    }
}
