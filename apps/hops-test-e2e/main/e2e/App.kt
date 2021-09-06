package e2e

import e2e.extensions.getRequired
import io.ktor.application.Application
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }
    install(ContentNegotiation) { json(Json { prettyPrint = true }) }
    install(CallLogging)

    routing {
        actuators(prometheus)
        trigger()
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/isReady") { call.respondText("ready") }
    get("/isAlive") { call.respondText("live") }
    get("/prometheus") { call.respond(prometheus.scrape()) }
}

private fun Routing.trigger() {
    post("/test") { application.runTests(HttpClient()) }

    get("/test") {
        val testExecutor = TestExecutor(HttpClient())
        testExecutor.runTests()
        call.respond(testExecutor.report)
    }
}

private suspend fun Application.runTests(httpClient: HttpClient) {
    val testExecutor = TestExecutor(httpClient)
    testExecutor.runTests()
    val report = testExecutor.report
    httpClient.sendResultsToWorkflow(environment.getRequired("api.github.base-url"), report)
}
