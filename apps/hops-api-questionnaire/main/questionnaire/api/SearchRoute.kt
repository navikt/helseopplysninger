package questionnaire.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import questionnaire.cache.QuestionnaireCache
import questionnaire.ktor.respondMissingParameter
import questionnaire.ktor.respondNotFound
import java.net.URI

fun Routing.search() {
    route("/4.0") {
        get("/questionnaire/{url}") {
            val url = call.parameters["url"] ?: return@get respondMissingParameter("url")

            val schema = when (url.hasVersion()) {
                true -> QuestionnaireCache.find(url.toURI(), url.version()) ?: return@get respondNotFound()
                false -> QuestionnaireCache.find(url.toURI()) ?: return@get respondNotFound()
            }

            call.respond(schema.raw)
        }
    }
}

fun String.hasVersion(): Boolean = contains("|")
fun String.version(): String = substringAfterLast("|")
fun String.toURI(): URI = URI(this)
