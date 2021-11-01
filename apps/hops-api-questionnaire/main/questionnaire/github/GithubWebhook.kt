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
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import mu.KotlinLogging
import questionnaire.fhir.FhirResourceFactory
import questionnaire.fhir.QuestionnaireEnricher
import questionnaire.github.Action.deleted
import questionnaire.store.QuestionnaireStore

private val log = KotlinLogging.logger {}

fun Application.githubWebhook(
    github: GithubApiClient,
) {
    suspend fun fetchAndCache(release: Release) = release.assets
        .map(Asset::browser_download_url)
        .map { github.getRelease(it) }
        .map(FhirResourceFactory::questionnaire)
        .map { QuestionnaireEnricher.enrich(release.created_at, it) }
        .forEach(QuestionnaireStore::add)

    routing {
        route("/github") {
            install(ContentNegotiation) { jackson() }

            /**
             * See repository settings for subscribed webhook events and registred webhook url
             */
            post("/event") {
                val webhook = call.receive<Webhook>()

                when (webhook.action) {
                    deleted -> log.warn { "Received deleted release event from webhook. Not implemented." }
                    else -> fetchAndCache(webhook.release)
                }
            }

            get("/ping") {
                call.respond(HttpStatusCode.NoContent, "pong")
            }
        }
    }
}
