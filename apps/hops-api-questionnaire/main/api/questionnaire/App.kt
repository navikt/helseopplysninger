package api.questionnaire

import api.questionnaire.github.GithubApiClient
import api.questionnaire.github.GithubReleaseCache
import api.questionnaire.github.githubWebhook
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfigsOrThrow<Config>("/application.yaml")

    install(CallLogging)
    install(Webjars)
    install(MicrometerMetrics) { registry = prometheus }

    val github = GithubApiClient(config.github)
    github.initReleaseCache()

    githubWebhook()

    routing {
        actuators(prometheus)
        swagger()
        read()
        search()
    }
}

private fun GithubApiClient.initReleaseCache() = runBlocking {
    val releases = getAllReleases()
    releases.forEach(GithubReleaseCache::add)
}
