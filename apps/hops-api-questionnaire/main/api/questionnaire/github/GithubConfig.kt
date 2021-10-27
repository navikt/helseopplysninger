package api.questionnaire.github

import java.net.URL

data class GithubConfig(
    val github: Github,
) {
    data class Github(
        val questionnaireUrl: URL,
    )
}
