package eventreplay

import eventreplay.domain.EventReplayJob
import eventreplay.infrastructure.Config
import eventreplay.infrastructure.EventStoreHttp
import eventreplay.infrastructure.FhirMessageStreamKafka
import eventreplay.infrastructure.HttpClientFactory
import eventreplay.routes.naisRoutes
import eventreplay.routes.smokeTestRoutes
import eventreplay.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
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
import no.nav.helse.hops.plugin.KafkaFactory

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val config = loadConfigsOrThrow<Config>()

    val kafkaProducer = KafkaFactory.createFhirProducer(config.kafka)
    val kafkaConsumer = KafkaFactory.createFhirConsumer(config.kafka)

    val kafka = FhirMessageStreamKafka(
        producer = kafkaProducer,
        consumer = kafkaConsumer,
        config = config.kafka,
    )

    val eventStoreClient = HttpClientFactory.create(config.eventStore)
    val eventStore = EventStoreHttp(config.eventStore, eventStoreClient)

    val replayJob = EventReplayJob(
        messageStream = kafka,
        log = log,
        eventStore = eventStore
    )

    environment.monitor.subscribe(ApplicationStopping) {
        replayJob.close()
        eventStoreClient.close()
        kafkaProducer.close()
        kafkaConsumer.close()
    }

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }

    routing {
        naisRoutes(replayJob, prometheusMeterRegistry)
        smokeTestRoutes(eventStore)
        swaggerRoutes()
    }
}
