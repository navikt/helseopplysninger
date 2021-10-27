package api.questionnaire.github

import api.questionnaire.github.Action.deleted
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun Application.githubWebhook(github: GithubApiClient) {

    routing {
        route("/github") {
            /**
             * See repository settings for subscribed webhook events and registred webhook url
             */
            post("/event") {
                val webhook = call.receive<Webhook>()

                when (webhook.action) {
                    deleted -> log.warn { "Received deleted release event from webhook. Not implemented." }
                    else -> github.cacheSchema(webhook.release)
                }
            }

            get("/ping") {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private suspend fun GithubApiClient.cacheSchema(release: Release) {
    release.assets
        .map(Asset::browser_download_url)
        .map { getRelease(it) }
        .forEach(QuestionnaireCache::add)
}
