package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.auth.configureAuthentication
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirJsonContentConverter
import no.nav.helse.hops.domain.HapiFacade
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.fhirRoutes
import no.nav.helse.hops.routes.naisRoutes
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
    install(ContentNegotiation) {
        register(ContentTypes.fhirJson, FhirJsonContentConverter())
    }
    install(Koin) {
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }

    routing {

        naisRoutes(prometheusMeterRegistry)
        fhirRoutes()
        authenticate {
            get("/") {
                call.respond(OK)
            }
        }
        // TODO auth
        val hapiTasks: HapiFacade by inject()
        get("/tasks") {
            val t = hapiTasks.tasks().firstOrNull()
            if (t != null) call.respond(t) else call.respond(OK)
        }
    }
}
