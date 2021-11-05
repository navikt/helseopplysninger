package questionnaire.github

import java.util.Date

data class Release(
    val assets: List<Asset>,
    val prerelease: Boolean,
    val created_at: Date,
)

data class Asset(
    val browser_download_url: String,
)
