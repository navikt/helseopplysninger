package fileshare

import fileshare.infrastructure.ApplicationServices
import fileshare.infrastructure.Config
import fileshare.routes.naisRoutes
import fileshare.routes.storageRoutes
import fileshare.routes.swaggerRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.helse.hops.security.AzureADProvider
import no.nav.helse.hops.security.HopsAuth
import no.nav.helse.hops.security.MaskinportenProvider

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    val config = loadConfigsOrThrow<Config>()
    val applicationServices = ApplicationServices(config)

    install(Webjars)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(HopsAuth) {
        providers += AzureADProvider(config.oauth.azure)
        providers += MaskinportenProvider(config.oauth.maskinporten)
    }

    routing {
        naisRoutes(prometheusMeterRegistry)
        swaggerRoutes()
        storageRoutes(applicationServices.fileSharingService)
    }
}
