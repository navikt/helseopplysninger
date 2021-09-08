package e2e

import e2e.extensions.GithubJson
import e2e.extensions.runTests
import e2e.extensions.sendDispatchEvent
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val config = loadConfigsOrThrow<Config>("/application.yml")

    install(MicrometerMetrics) { registry = prometheus }
    install(DefaultHeaders)
    install(CallLogging)

    routing {
        actuators(prometheus)
        e2eTrigger(config)
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/isReady") { call.respondText("ready") }
    get("/isAlive") { call.respondText("live") }
    get("/prometheus") { call.respond(prometheus.scrape()) }
}

private fun Routing.e2eTrigger(config: Config) {
    val hops = HttpClient()
    val github = HttpClient {
        install(JsonFeature) {
            acceptContentTypes = listOf(GithubJson)
            serializer = KotlinxSerializer()
        }
    }

    post("/runTests") {
        val githubBaseUrl = config.api.github.baseUrl
        val results = hops.runTests(config.api.hops)
        github.sendDispatchEvent(githubBaseUrl, results)

        call.respondText("Tests are now running..", status = HttpStatusCode.Accepted)
    }
}
