package questionnaire.api.search

import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import questionnaire.cache.Cache
import questionnaire.fhir.BundleFactory
import questionnaire.ktor.respondMissingParameter
import java.net.URI

fun Routing.search() {
    route("/4.0") {
        get("/questionnaire") {
            val url = call.parameters["url"] ?: return@get respondMissingParameter("url")

            val questionnaires = when (url.hasVersion()) {
                true -> Cache.search(url.toURI(), url.version())
                false -> Cache.search(url.toURI())
            }
            val searchset = BundleFactory.searchset(questionnaires)

            call.respond(searchset)
        }

        post("/questionnaire/_search") {
            val params = call.receiveParameters()
            val url = params["url"]?.toURI()
            val version = params["version"]

            val questionnaires = Cache.search(url, version)
            val searchset = BundleFactory.searchset(questionnaires)

            call.respond(searchset)
        }
    }
}

fun String.hasVersion(): Boolean = contains("|")
fun String.version(): String = substringAfterLast("|")
fun String.toURI(): URI = URI(this)
