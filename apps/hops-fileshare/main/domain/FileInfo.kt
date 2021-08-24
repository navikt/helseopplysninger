package domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
    val name: String,
    val contentType: String,
    val md5Hash: String,
    val created: Instant
)
