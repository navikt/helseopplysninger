package fileshare

import fileshare.infrastructure.ApplicationServices
import fileshare.infrastructure.Config
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
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val config = loadConfigsOrThrow<Config>("/application.yaml")
    val applicationServices = ApplicationServices(config)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(Authentication) { useNaviktTokenSupport(config.oauth) }

    routing {
        naisRoutes(prometheusMeterRegistry)
        swaggerRoutes()
        storageRoutes(applicationServices.fileSharingService)
    }
}
