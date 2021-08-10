import infrastructure.KoinBootstrapper
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import routes.naisRoutes
import routes.smokeTestRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CallLogging)
    install(Webjars)
    install(Koin) {
        slf4jLogger()
        modules(listOf(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule()))
    }

    routing {
        naisRoutes()
        smokeTestRoutes()
        swaggerRoutes()
    }
}
