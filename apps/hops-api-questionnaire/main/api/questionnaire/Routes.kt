package api.questionnaire

import api.questionnaire.github.GithubReleaseCache
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.read() {
    route("/4.0") {
        get("/questionnaire/{id}") {
            when (val id = call.parameters["id"]) {
                null -> call.respondText("Missing required parameter 'id'", status = BadRequest)
                else -> when (val schema = GithubReleaseCache.get(id)) {
                    null -> call.respondText("id $id not found", status = HttpStatusCode.NotFound)
                    else -> call.respondText(schema, contentType = ContentType.Application.Json)
                }
            }
        }
    }
}

fun Routing.search() {
    route("/4.0") {
        get("/questionnaire/{url}") {
            when (val url = call.parameters["url"]) {
                null ->
                if (url.contains("|"))

            }
        }
    }
}

fun Routing.swagger() {
    static("static") {
        resources("web")
    }
    get("/") {
        call.respondRedirect("/webjars/swagger-ui/index.html?url=/static/openapi.yaml")
    }
}

fun Routing.actuators(prometheusMeterRegistry: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/ready") { call.respond("ready") }
        get("/live") { call.respondText("live") }
        get("/metrics") { call.respond(prometheusMeterRegistry.scrape()) }
    }
}
