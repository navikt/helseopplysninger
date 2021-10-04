package archive

import archive.domain.EventSinkJob
import archive.infrastructure.Config
import archive.infrastructure.ArchiveHttp
import archive.infrastructure.FhirMessageBusKafka
import archive.infrastructure.HttpClientFactory
import archive.infrastructure.KafkaFactory
import archive.routes.naisRoutes
import archive.routes.smokeTestRoutes
import archive.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }

    val config = loadConfigsOrThrow<Config>("/application.yaml")
    val archive = ArchiveHttp(config.eventStore, HttpClientFactory.create(config.eventStore))
    val kafkaConsumer = FhirMessageBusKafka(KafkaFactory.createFhirConsumer(config.kafka), config.kafka)
    val fhirSink = EventSinkJob(kafkaConsumer, log, archive)

    routing {
        naisRoutes(fhirSink, prometheusMeterRegistry)
        smokeTestRoutes(archive)
        swaggerRoutes()
    }
}
