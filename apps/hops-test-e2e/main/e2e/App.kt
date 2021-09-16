package e2e

import e2e._common.E2eExecutor
import e2e._common.e2eExecutor
import e2e.replay.replayTests
import e2e.api.apiTests
import e2e.sink.sinkTests
import e2e.store.storeTests
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
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
import io.ktor.util.pipeline.PipelineContext
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import kotlin.time.ExperimentalTime

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(ContentNegotiation) { json(Json { prettyPrint = true }) }
    install(MicrometerMetrics) { registry = prometheus }
    install(DefaultHeaders)
    install(CallLogging)

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
        log.info("Running all tests...")

        val result = e2e.exec()

        log.info("${e2e.size} tests completed in ${result.totalDurationMs}")

        call.respond(result)
    }
}

inline val PipelineContext<*, ApplicationCall>.log: Logger get() = application.log

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/actuator/ready") { call.respondText("ready") }
    get("/actuator/live") { call.respondText("live") }
    get("/metrics") { call.respond(prometheus.scrape()) }
}
