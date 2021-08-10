import infrastructure.KoinBootstrapper
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import routes.naisRoutes
import routes.smokeTestRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(Koin) {
        slf4jLogger()
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }

    routing {
        naisRoutes(prometheusMeterRegistry)
        smokeTestRoutes()
        swaggerRoutes()
    }
}
