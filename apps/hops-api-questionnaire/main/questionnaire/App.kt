package questionnaire

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
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import questionnaire.api.actuators
import questionnaire.api.read
import questionnaire.api.search
import questionnaire.api.swagger
import questionnaire.cache.QuestionnaireCache
import questionnaire.github.GithubApiClient
import questionnaire.github.githubWebhook

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
    QuestionnaireCache.initiate(github)
    githubWebhook(github)

    routing {
        actuators(prometheus)
        swagger()
        read()
        search()
    }
}
