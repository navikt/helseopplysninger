import domain.FhirMessageProcessService
import domain.FhirMessageSearchService
import infrastructure.EventStoreConfig
import infrastructure.EventStoreRepositoryExposedORM
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
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.diagnostics.useRequestIdHeader
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage
import no.nav.security.token.support.ktor.tokenValidationSupport
import routes.fhirRoutes
import routes.naisRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    val meterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Authentication) { tokenValidationSupport(config = environment.config) }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging)
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(MicrometerMetrics) { registry = meterRegistry }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Webjars)
    install(XForwardedHeaderSupport)

    val config = loadConfigsOrThrow<EventStoreConfig>("/application.conf", "/application.properties")
    val repo = EventStoreRepositoryExposedORM(config.db)
    val processService = FhirMessageProcessService(repo)
    val searchService = FhirMessageSearchService(repo)

    routing {
        swaggerRoutes()
        naisRoutes(searchService, meterRegistry)
        fhirRoutes(searchService, processService)
    }
}
