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
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
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
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

data class Config(val api: Api) {
    data class Api(val github: Github, val internal: Internal)
    data class Github(val baseUrl: String)
    data class Internal(val domain: String)
}

fun Application.main() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }
    install(DefaultHeaders)
    install(ContentNegotiation) { json(Json { prettyPrint = true }) } // kotlinx.serialization
    install(CallLogging)

    routing {
        actuators(prometheus)
        e2eTrigger()
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    get("/isReady") { call.respondText("ready") }
    get("/isAlive") { call.respondText("live") }
    get("/prometheus") { call.respond(prometheus.scrape()) }
}

private fun Routing.e2eTrigger() {
    val config = application.loadConfigsOrThrow<Config>()
    val internalHttp = HttpClient()
    val githubHttp = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
            accept(GithubJson)
        }
    }

    post("/runTests") {
        val internalDomain = config.api.internal.domain
        val githubBaseUrl = config.api.github.baseUrl
        val results = internalHttp.runTests(internalDomain)
        githubHttp.sendDispatchEvent(githubBaseUrl, results)

        call.respondText("Tests are now running..", status = HttpStatusCode.Accepted)
    }
}
