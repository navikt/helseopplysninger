package questionnaire.api

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import questionnaire.fhir.FhirResourceFactory
import questionnaire.ktor.respondNotFound
import questionnaire.store.QuestionnaireStore
import java.net.URI

fun Routing.questionnaire() {
    route("/4.0") {
        get("/questionnaire/{id}") {
            val id = call.parameters["id"]!!
            val schema = QuestionnaireStore.get(id) ?: return@get respondNotFound()
            call.respond(schema)
        }

        get("/questionnaire") {
            val url = call.parameters["url"]
            val version = url?.version()

            val uri = when (version != null) {
                true -> url.substringBefore("|").toURI()
                false -> url?.toURI()
            }

            searchAndRespond(uri, version)
        }

        post("/questionnaire/_search") {
            val params = call.receiveParameters()
            val url = params["url"]?.toURI()
            val version = params["version"]

            searchAndRespond(url, version)
        }
    }
}

private fun String.toURI(): URI = URI(this)

private suspend fun PipelineContext<Unit, ApplicationCall>.searchAndRespond(url: URI?, version: String?) {
    val questionnaires = QuestionnaireStore.search(url, version)
    val searchset = FhirResourceFactory.searchset(questionnaires)
    call.respond(searchset)
}

private fun String.version(): String? {
    require((count { it == '|' }) <= 1) // nested pipe is not supported

    return when (contains("|")) {
        true -> substringAfter("|")
        false -> null
    }
}
