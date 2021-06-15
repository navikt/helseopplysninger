package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.http.HttpHeaders
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.naisRoutes
import no.nav.helse.hops.routes.smokeTestRoutes
import no.nav.helse.hops.routes.swaggerRoutes
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import java.util.UUID

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    install(Koin) {
        slf4jLogger()
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
    }

    routing {
        naisRoutes(prometheusMeterRegistry)
        smokeTestRoutes()
        swaggerRoutes()
    }
}
