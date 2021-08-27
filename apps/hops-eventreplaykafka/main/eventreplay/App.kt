package eventreplay

import eventreplay.domain.EventReplayJob
import eventreplay.infrastructure.Config
import eventreplay.infrastructure.EventStoreHttp
import eventreplay.infrastructure.FhirMessageBusKafka
import eventreplay.infrastructure.HttpClientFactory
import eventreplay.infrastructure.KafkaFactory
import eventreplay.routes.naisRoutes
import eventreplay.routes.smokeTestRoutes
import eventreplay.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val config = loadConfigsOrThrow<Config>()

    val kafka = FhirMessageBusKafka(
        producer = KafkaFactory.createFhirProducer(config.kafka),
        consumer = KafkaFactory.createFhirConsumer(config.kafka),
        config = config.kafka,
    )

    val eventStore = EventStoreHttp(config.eventStore, HttpClientFactory.create(config.eventStore))
    val replayJob = EventReplayJob(
        messageBus = kafka,
        log = log,
        eventStore = eventStore,
    )

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }

    routing {
        naisRoutes(replayJob, prometheusMeterRegistry)
        smokeTestRoutes(eventStore)
        swaggerRoutes()
    }
}