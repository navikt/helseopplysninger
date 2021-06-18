package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.config.ApplicationConfig
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.diagnostics.useRequestIdHeader
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.fhirRoutes
import no.nav.helse.hops.routes.naisRoutes
import no.nav.helse.hops.routes.swaggerRoutes
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Authentication) { useMaskinporten(environment.config) }
    install(CallId) { useRequestIdHeader() }
    install(CallLogging)
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Webjars)
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

private fun Authentication.Configuration.useMaskinporten(config: ApplicationConfig) {
    val rc = RequiredClaims(
        "maskinporten",
        arrayOf("scope=nav:helse/v1/helseopplysninger")
    )
    tokenValidationSupport(config = config, requiredClaims = rc)
}
