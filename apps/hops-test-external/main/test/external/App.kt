package test.external

import test.external.infrastructure.ExternalApiHttp
import test.external.infrastructure.HttpClientFactory
import test.external.infrastructure.TestExternalConfig
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import test.external.routes.naisRoutes
import test.external.routes.smokeTestRoutes
import test.external.routes.swaggerRoutes

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
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
