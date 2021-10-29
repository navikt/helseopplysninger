package questionnaire.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import questionnaire.cache.Cache
import questionnaire.ktor.respondMissingParameter
import questionnaire.ktor.respondNotFound

fun Routing.read() {
    route("/4.0") {
        get("/questionnaire/{id}") {
            val id = call.parameters["id"] ?: return@get respondMissingParameter("id")
            val schema = Cache.get(id) ?: return@get respondNotFound()
            call.respond(schema.raw)
        }
    }
}
