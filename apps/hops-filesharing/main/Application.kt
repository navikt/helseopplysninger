import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import no.nav.security.token.support.ktor.tokenValidationSupport
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import routes.naisRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(Authentication) { tokenValidationSupport(config = environment.config) }

    routing {
        naisRoutes(prometheusMeterRegistry)
        swaggerRoutes()
    }
}
