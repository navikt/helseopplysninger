package api.questionnaire.github

object Release {
    data class Releases(val releases: List<Release>)

    data class Release(
        val prerelease: Boolean,
        val assets: List<Asset>,
    )

    data class Asset(val browser_download_url: String)
}

object Webhook {
    data class Webhook(
        val zen: String,
        val hook_id: Int,
        val hook: WebhookConfig,
        val repository: Repository,
        val organization: Organization,
        val sender: Sender,
    )

    data class WebhookConfig(
        val type: String,
    )

    data class Repository(
        val name: String,
        val full_name: String,
        val html_url: String,
        val fork: Boolean,
    )

    data class Organization(
        val name: String,
    )

    data class Sender(
        val login: String, // the username
        val type: String, // User
    )
}
