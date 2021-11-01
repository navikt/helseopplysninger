package dialogmelding

import dialogmelding.routes.naisRoutes
import dialogmelding.routes.swaggerRoutes
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
import no.nav.helse.hops.plugin.MessageStreamKafka

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }

    val config = loadConfigsOrThrow<Config>()

    val pdfConverterClient = HttpClientFactory.create(config.fhirJsonToPdfConverter)
    val kafkaConsumer = KafkaFactory.createFhirConsumer(config.kafka)

    val pdfConverter = FhirJsonToPdfConverter(config.fhirJsonToPdfConverter, pdfConverterClient)
    val messageStream = MessageStreamKafka(kafkaConsumer, config.kafka.topic)
    val mqSender = MQSender(config.messageQueue)

    val job = DialogmeldingJob(messageStream, log, pdfConverter, mqSender)

    environment.monitor.subscribe(ApplicationStopping) {
        job.close()
        pdfConverterClient.close()
        kafkaConsumer.close()
    }

    routing {
        naisRoutes(job, prometheusMeterRegistry)
        swaggerRoutes()
    }
}
