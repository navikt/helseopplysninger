package eventsink

import eventsink.domain.EventSinkJob
import eventsink.infrastructure.Config
import eventsink.infrastructure.EventStoreHttp
import eventsink.infrastructure.HttpClientFactory
import eventsink.routes.naisRoutes
import eventsink.routes.smokeTestRoutes
import eventsink.routes.swaggerRoutes
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
import no.nav.helse.hops.plugin.MessageStreamKafka
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
    val fhirStore = EventStoreHttp(config.eventStore, HttpClientFactory.create(config.eventStore))
    val kafkaConsumer = MessageStreamKafka(KafkaFactory.createFhirConsumer(config.kafka), config.kafka.topic)
    val fhirSink = EventSinkJob(kafkaConsumer, log, fhirStore, environment.parentCoroutineContext)

    routing {
        naisRoutes(fhirSink, prometheusMeterRegistry)
        smokeTestRoutes(fhirStore)
        swaggerRoutes()
    }
}
