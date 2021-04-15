package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.fkr.FkrFacade
import no.nav.helse.hops.fkr.FkrKoinModule
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT) // https://ktor.io/docs/micrometer-metrics.html
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        modules(
            module { single { environment.config } }, // makes the configuration available to DI.
            FkrKoinModule.instance
        )
    }
    install(Authentication) {
        tokenValidationSupport(config = environment.config)
    }
    routing {
        get("/isReady") {
            call.respondText("ready")
        }
        get("/isAlive") {
            call.respondText("alive")
        }
        get("/prometheus") {
            call.respond(appMicrometerRegistry.scrape())
        }

        authenticate {
            val fkr: FkrFacade by inject()
            get("/behandler/{hprNr}") {
                call.parameters["hprNr"]?.toIntOrNull()?.let {
                    val name = fkr.practitionerName(it)
                    call.respondText(name)
                }
            }
        }
    }
}
