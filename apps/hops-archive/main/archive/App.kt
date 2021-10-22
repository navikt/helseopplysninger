package archive

import archive.routes.naisRoutes
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
import no.nav.helse.hops.plugin.FhirMessageStreamKafka
import no.nav.helse.hops.plugin.KafkaFactory

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }

    val config = loadConfigsOrThrow<Config>("/application.yaml")
    val archive = Dokarkiv(config.dokarkiv, HttpClientFactory.create(config.dokarkiv))
    val pdfConverter = FhirJsonToPdfConverter(config.fhirJsonToPdfConverter, HttpClientFactory.create(config.fhirJsonToPdfConverter))
    val kafkaConsumer = FhirMessageStreamKafka(KafkaFactory.createFhirConsumer(config.kafka), config.kafka.topic)

    val job = ArchiveJob(kafkaConsumer, log, archive, pdfConverter, environment.parentCoroutineContext)

    routing {
        naisRoutes(job, prometheusMeterRegistry)
        swaggerRoutes()
    }
}
