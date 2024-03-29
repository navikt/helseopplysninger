package questionnaire

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.content.JsonFhirR4
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import questionnaire.api.actuators
import questionnaire.api.questionnaire
import questionnaire.api.swagger
import questionnaire.github.GithubApiClient
import questionnaire.github.githubEvents

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfigsOrThrow<Config>()
    val github = GithubApiClient(config.github.api)

    install(CallLogging)
    install(Webjars)
    install(MicrometerMetrics) { registry = prometheus }
    install(ContentNegotiation) {
        register(ContentType.Application.JsonFhirR4, FhirR4JsonContentConverter())
    }

    routing {
        actuators(prometheus)
        githubEvents(github)
        questionnaire()
        swagger()
    }
}
