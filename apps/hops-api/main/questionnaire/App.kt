package questionnaire

import questionnaire.infrastructure.Config
import questionnaire.infrastructure.EventStoreHttp
import questionnaire.infrastructure.HttpClientFactory
import questionnaire.routes.fhirRoutes
import questionnaire.routes.naisRoutes
import questionnaire.routes.smokeTestRoutes
import questionnaire.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import mu.KotlinLogging
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.diagnostics.useRequestIdHeader
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.helse.hops.security.HopsAuth
import no.nav.helse.hops.security.MaskinportenProvider
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage

private val log = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = loadConfigsOrThrow<Config>("/application.yaml")
    log.debug { "Running configuration: $config" }

    val httpClient = HttpClientFactory.create(config.eventStore)
    val eventStoreClient = EventStoreHttp(httpClient, config.eventStore)
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(HopsAuth) {
        providers += MaskinportenProvider(config.oauth.maskinporten)
    }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging) { logger = log }
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Webjars)
    install(XForwardedHeaderSupport)

    routing {
        fhirRoutes(eventStoreClient)
        naisRoutes(prometheusMeterRegistry)
        smokeTestRoutes(eventStoreClient)
        swaggerRoutes()
    }
}
