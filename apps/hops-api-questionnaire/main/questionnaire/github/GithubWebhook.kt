package questionnaire.github

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun Application.githubWebhook(github: GithubApiClient) {
    routing {
        install(ContentNegotiation) { jackson() }

        get("/github/event") {
            val event = call.receive<Webhook>()
            log.info { "Triggered from github event" }
            log.info { event }
            github.fetchAndStoreLatest()
        }

        get("/github/ping") {
            call.respond(HttpStatusCode.NoContent, "pong")
        }
    }
}
