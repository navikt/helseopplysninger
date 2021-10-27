package api.questionnaire

import api.questionnaire.github.GithubApiClient
import api.questionnaire.github.QuestionnaireCache
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
    initReleaseCache(github)
    githubWebhook(github)

    routing {
        actuators(prometheus)
        swagger()
        read()
        search()
    }
}

/**
 * Populate the in memory cache for every releases available on github.com/navikt/fhir-questionnaire
 */
private fun initReleaseCache(github: GithubApiClient) = runBlocking {
    val releaseUrls = github.getAllReleaseUrls()
    releaseUrls.forEach { url ->
        val release = github.getRelease(url)
        QuestionnaireCache.add(release)
    }
}
