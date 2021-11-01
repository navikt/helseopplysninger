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
import questionnaire.store.QuestionnaireStore
import java.net.URI

fun Routing.search() {
    route("/4.0") {
        get("/questionnaire") {
            val url = call.parameters["url"]?.toURI()
            val version = url?.version()
            searchAndRespond(url, version)
        }

        post("/questionnaire/_search") {
            val params = call.receiveParameters()
            val url = params["url"]?.toURI()
            val version = params["version"]
            searchAndRespond(url, version)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchAndRespond(
    url: URI?,
    version: String?
) {
    val questionnaires = QuestionnaireStore.search(url, version)
    val searchset = FhirResourceFactory.searchset(questionnaires)

    call.respond(searchset)
}

private fun String.toURI(): URI = URI(this)
private fun URI.version(): String? = when (toString().contains("|")) { // TODO: test at tostring er riktig her
    true -> toString().substringAfterLast("|")
    false -> null
}
