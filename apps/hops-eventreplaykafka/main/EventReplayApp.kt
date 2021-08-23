import domain.EventReplayJob
import infrastructure.EventReplayKafkaConfig
import infrastructure.EventStoreHttp
import infrastructure.FhirMessageBusKafka
import infrastructure.HttpClientFactory
import infrastructure.KafkaFactory
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
import routes.naisRoutes
import routes.smokeTestRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val config = loadConfigsOrThrow<EventReplayKafkaConfig>()

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
