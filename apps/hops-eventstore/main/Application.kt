import infrastructure.KoinBootstrapper
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
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import routes.fhirRoutes
import routes.naisRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Authentication) { tokenValidationSupport(config = environment.config) }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging)
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Webjars)
    install(XForwardedHeaderSupport)
    install(Koin) {
        slf4jLogger()
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }

    routing {
        swaggerRoutes()
        naisRoutes(prometheusMeterRegistry)
        fhirRoutes()
    }
}
