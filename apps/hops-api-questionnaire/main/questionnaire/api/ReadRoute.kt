package questionnaire.api

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import mu.KotlinLogging
import questionnaire.cache.QuestionnaireCache
import questionnaire.ktor.respondMissingParameter
import questionnaire.ktor.respondNotFound

private val log = KotlinLogging.logger {}

fun Routing.read() {
    route("/4.0") {
        get("/questionnaire/{id}") {

            debug()

            val id = call.parameters["id"] ?: return@get respondMissingParameter("id")
            val schema = QuestionnaireCache.find(id) ?: return@get respondNotFound()
            call.respond(schema.raw)
        }
    }
}

@Deprecated("DELETE ME")
private fun PipelineContext<Unit, ApplicationCall>.debug() {
    if (call.request.queryParameters["_format"] != null) log.warn { "Requested with parameter _format" }
    if (call.request.queryParameters["_pretty"] != null) log.warn { "Requested with parameter _pretty" }
    if (call.request.queryParameters["_summary"] != null) log.warn { "Requested with parameter _summary" }
    if (call.request.queryParameters["_elements"] != null) log.warn { "Requested with parameter _elements" }
}
