package api

import api.infrastructure.Config
import api.infrastructure.EventStoreHttp
import api.infrastructure.HttpClientFactory
import api.infrastructure.useNaviktTokenSupport
import api.routes.fhirRoutes
import api.routes.naisRoutes
import api.routes.smokeTestRoutes
import api.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
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
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.diagnostics.useRequestIdHeader
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = loadConfigsOrThrow<Config>("/application.yaml")
    val httpClient = HttpClientFactory.create(config.eventStore)
    val eventStoreClient = EventStoreHttp(httpClient, config.eventStore)
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Authentication) { useNaviktTokenSupport(config.oauth) }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging)
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
