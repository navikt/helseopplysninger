package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.convert.FhirR4JsonContentConverter
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import no.nav.helse.hops.routes.fhirRoutes
import no.nav.helse.hops.routes.naisRoutes
import no.nav.helse.hops.routes.swaggerRoutes
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.OperationOutcome
import org.koin.ktor.ext.Koin
import java.time.LocalDateTime

@Suppress("unused") // Referenced in application.conf
fun Application.api() {
    install(Webjars)
    install(DefaultHeaders)
    install(CallLogging)
    val prometheusMeterRegistry = PrometheusMeterRegistry(DEFAULT)
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    install(ContentNegotiation) {
        register(ContentTypes.fhirJson, FhirR4JsonContentConverter())
    }
    install(Koin) {
        modules(KoinBootstrapper.module, environment.config.asHoplitePropertySourceModule())
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            val outcome = OperationOutcome().apply {
                meta = Meta().apply { lastUpdated = LocalDateTime.now().toUtilDate() }

                val issue = OperationOutcome.OperationOutcomeIssueComponent()
                issue.apply {
                    severity = OperationOutcome.IssueSeverity.ERROR
                    code = OperationOutcome.IssueType.EXCEPTION
                    diagnostics = cause.message
                }

                addIssue(issue)
            }
            call.respond(InternalServerError, outcome)
        }
    }

    routing {
        swaggerRoutes()
        naisRoutes(prometheusMeterRegistry)
        fhirRoutes()
    }
}
