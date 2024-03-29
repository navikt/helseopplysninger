package eventstore

import eventstore.domain.FhirMessageProcessService
import eventstore.domain.FhirMessageSearchService
import eventstore.infrastructure.Config
import eventstore.infrastructure.EventStoreRepositoryExposedORM
import eventstore.routes.fhirRoutes
import eventstore.routes.naisRoutes
import eventstore.routes.swaggerRoutes
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
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.diagnostics.useRequestIdHeader
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.helse.hops.security.AzureADProvider
import no.nav.helse.hops.security.HopsAuth
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = loadConfigsOrThrow<Config>()
    val meterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(HopsAuth) {
        providers += AzureADProvider(config.oauth.azure)
    }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging)
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(MicrometerMetrics) { registry = meterRegistry }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Webjars)
    install(XForwardedHeaderSupport)

    val repo = EventStoreRepositoryExposedORM(config.db)
    val processService = FhirMessageProcessService(repo)
    val searchService = FhirMessageSearchService(repo)

    routing {
        swaggerRoutes()
        naisRoutes(searchService, meterRegistry)
        fhirRoutes(searchService, processService)
    }
}
