package archive.routes

import archive.ArchiveJob
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.naisRoutes(
    job: ArchiveJob,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    route("/actuator") {
        get("/ready") {
            val statusCode = if (job.isRunning) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(statusCode, "Archive")
        }
        get("/alive") {
            call.respondText("Archive")
        }
        get("/prometheus") {
            call.respond(prometheusMeterRegistry.scrape())
        }
    }
}
