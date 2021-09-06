package fileshare

import fileshare.infrastructure.ApplicationServices
import fileshare.infrastructure.useNaviktTokenSupport
import fileshare.routes.naisRoutes
import fileshare.routes.storageRoutes
import fileshare.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.security.token.support.ktor.tokenValidationSupport

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val applicationServices = ApplicationServices(loadConfigsOrThrow())

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(Authentication) { useNaviktTokenSupport(config = environment.config) }

    routing {
        naisRoutes(prometheusMeterRegistry)
        swaggerRoutes()
        storageRoutes(applicationServices.fileSharingService)
    }
}
