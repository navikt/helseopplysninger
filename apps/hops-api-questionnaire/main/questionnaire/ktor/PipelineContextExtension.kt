package questionnaire.ktor

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.respondMissingParameter(param: String) {
    call.respondText("Missing parameter '$param'", status = HttpStatusCode.BadRequest)
}

suspend fun PipelineContext<Unit, ApplicationCall>.respondNotFound() {
    call.respondText("Questionnaire not found", status = HttpStatusCode.NotFound)
}
