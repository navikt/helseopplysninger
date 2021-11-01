package questionnaire.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import questionnaire.ktor.respondNotFound
import questionnaire.store.QuestionnaireStore

fun Routing.read() {
    route("/4.0") {
        get("/questionnaire/{id}") {
            val id = call.parameters["id"]!!
            val schema = QuestionnaireStore.get(id) ?: return@get respondNotFound()
            call.respond(schema)
        }
    }
}
