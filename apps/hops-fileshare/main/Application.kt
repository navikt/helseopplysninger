import infrastructure.ApplicationServices
import infrastructure.FileShareConfig
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
import routes.naisRoutes
import routes.storageRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val applicationConfig = loadConfigsOrThrow<FileShareConfig>("/application.conf", "/application.properties")
    val applicationServices = ApplicationServices(applicationConfig)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(Authentication) { tokenValidationSupport(config = environment.config) }

    routing {
        naisRoutes(prometheusMeterRegistry)
        swaggerRoutes()
        storageRoutes(applicationServices.fileSharingService)
    }
}
