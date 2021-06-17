package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.firstOrNull
import no.nav.helse.hops.auth.configureAuthentication
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.fhirRoutes
import no.nav.helse.hops.routes.naisRoutes
import no.nav.helse.hops.statuspages.useFhirErrorStatusPage
import org.hl7.fhir.r4.model.Task
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    configureAuthentication()

    install(DefaultHeaders)
    install(CallLogging)
    install(MicrometerMetrics) { registry = prometheusMeterRegistry }
    install(ContentNegotiation) { register(ContentTypes.fhirJson, FhirR4JsonContentConverter()) }
    install(StatusPages) { useFhirErrorStatusPage() }
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
        val hapi: FhirClientReadOnly by inject()
        get("/tasks") {
            val task = hapi.search<Task>().firstOrNull()
            if (task != null) call.respond(task) else call.respond(OK)
        }
    }
}
