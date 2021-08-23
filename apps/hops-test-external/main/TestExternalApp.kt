import infrastructure.ExternalApiHttp
import infrastructure.HttpClientFactory
import infrastructure.TestExternalConfig
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import routes.naisRoutes
import routes.smokeTestRoutes
import routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CallLogging)
    install(Webjars)

    val config = loadConfigsOrThrow<TestExternalConfig>()
    val externalApi = ExternalApiHttp(HttpClientFactory.create(config.externalApi), config.externalApi)

    routing {
        naisRoutes()
        smokeTestRoutes(externalApi)
        swaggerRoutes()
    }
}
