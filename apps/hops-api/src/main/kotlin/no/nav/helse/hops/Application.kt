package no.nav.helse.hops

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
import org.hl7.fhir.instance.model.api.IBaseResource
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
    install(Koin) {
        modules(KoinBootstrapper.module)
    }
    routing {
        naisRoutes(prometheusMeterRegistry)
        authenticate {
            get("/") {
                call.respond(OK)
            }
        }
        // TODO auth
        val hapiTasks: HapiFacade by inject()
        get("/tasks") {
            val t = hapiTasks.tasks().firstOrNull()
            t?.toJson()?.let { it1 -> call.respondText(it1) }
        }
        get("/hello") {
            call.respond("Hello")
        }

    }
}
fun IBaseResource.toJson(): String {
    val ctx = FhirContext.forCached(FhirVersionEnum.R4)!!
    val parser = ctx.newJsonParser()!!
    return parser.encodeResourceToString(this)
}
