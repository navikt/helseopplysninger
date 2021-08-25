package fileshare.domain

import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import java.util.UUID

interface VirusScanner {
    suspend fun prepareForScan(content: ByteReadChannel, contentType: ContentType, fileName: String = UUID.randomUUID().toString()): FileInfo
    suspend fun scan(fileName: String): ByteReadChannel
}
