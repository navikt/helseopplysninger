package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.auth.configureAuthentication
import no.nav.helse.hops.domain.HapiFacade
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.naisRoutes
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    install(DefaultHeaders)
    install(CallLogging)
    configureAuthentication()
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        modules(KoinBootstrapper.module)
    }
    install(Authentication) {
        tokenValidationSupport(config = environment.config)
    }
    routing {
        naisRoutes(prometheusMeterRegistry)
        authenticate {
            get("/") {
                call.respond(OK)
            }

            // Call hops-fhir-server to get all tasks
            val hapiTasks: HapiFacade by inject()
            get("/tasks") {
                call.respondText { "getTasks probably not implemented :-)" }
                for (item in hapiTasks.tasks())
                    call.respondText(item.toString())
            }
        }
    }
}
