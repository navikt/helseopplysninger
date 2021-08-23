package test.external.routes

import test.external.domain.ExternalApiFacade
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.smokeTestRoutes(externalApi: ExternalApiFacade) {
    route("/smokeTests") {
        get("/external-api") {
            try {
                externalApi.get()
                call.respondText { "OK!" }
            } catch (ex: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ex.message ?: "No exception message."
                )
            }
        }
    }
}
