package questionnaire.github

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.header
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun Application.githubEvents(github: GithubApiClient) {
    routing {
        install(ContentNegotiation) { jackson() }

        post("/github/event") {
            log.debug(call.receiveText())

            suspend fun processRelease() {
                github.fetchAndStoreLatest()
                call.respond(HttpStatusCode.Accepted, "Lastest release fetched and stored")
            }

            when (call.request.header("X-GitHub-Event")!!) {
                "release" -> processRelease()
                "ping" -> call.respond(HttpStatusCode.NoContent, "")
                else -> call.respond(HttpStatusCode.UnprocessableEntity, "Event ignored")
            }
        }
    }
}
