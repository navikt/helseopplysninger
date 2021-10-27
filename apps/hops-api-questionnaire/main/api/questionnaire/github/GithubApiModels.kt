package api.questionnaire.github

data class Webhook(val action: Action, val release: Release)
data class Release(val assets: List<Asset>)
data class Asset(val browser_download_url: String)

enum class Action {
    /**
     * A release, pre-release or draft of a release is published
     */
    published,

    /**
     * A release or pre-release is deleted
     */
    unpublished,

    /**
     * A draft is saved,
     * or a release or pre-release is published without previously being saved as a draft
     */
    created,

    /**
     * A release, pre-release or draft release is edited
     */
    edited,

    /**
     * A release, pre-release or draft is deleted
     */
    deleted,

    /**
     * A pre-release is created
     */
    prereleased,

    /**
     * A release or draft of a release is published,
     * or a pre-release is changed to a release
     */
    released,
}
