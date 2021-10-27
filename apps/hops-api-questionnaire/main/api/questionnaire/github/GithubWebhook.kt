package api.questionnaire.github

import io.ktor.application.Application
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

fun Application.githubWebhook() {

    routing {
        route("/webhook/github") {
            get("/ping") {

            }

            get("/event") {

            }
        }
    }
}
