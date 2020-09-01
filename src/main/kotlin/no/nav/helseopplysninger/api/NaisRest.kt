package no.nav.helseopplysninger.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helseopplysninger.ApplicationState

fun Routing.registerNaisApi(
        applicationState: ApplicationState,
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    DefaultExports.initialize()

    get("/isAlive") {
        if (applicationState.running) {
            call.respondText("Application is alive")
        } else {
            call.respondText("Application is dead", status = HttpStatusCode.InternalServerError)
        }
    }

    get("/isReady") {
        if (applicationState.initialized) {
            call.respondText("Application is ready")
        } else {
            call.respondText("Application is not ready", status = HttpStatusCode.InternalServerError)
        }
    }

    get("/prometheus") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }

}
