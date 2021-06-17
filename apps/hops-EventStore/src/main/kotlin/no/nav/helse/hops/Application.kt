package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DoubleReceive
import io.ktor.features.RejectedCallIdException
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.fhirRoutes
import no.nav.helse.hops.routes.naisRoutes
import no.nav.helse.hops.routes.swaggerRoutes
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import java.util.UUID

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(DoubleReceive) { receiveEntireContent = true }
    install(Authentication) { tokenValidationSupport(config = environment.config) }
    install(StatusPages) { useFhirErrorStatusPage() }
    install(Koin) {
        slf4jLogger()
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
        verify { if (it.length > 200) throw RejectedCallIdException(it) else it.isNotBlank() }
    }

    routing {
        swaggerRoutes()
        naisRoutes(prometheusMeterRegistry)
        fhirRoutes()
    }
}
