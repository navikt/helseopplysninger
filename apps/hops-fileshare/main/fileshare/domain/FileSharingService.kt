package fileshare.domain

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

class FileSharingService(
    private val virusScanner: VirusScanner,
    private val fileStore: FileStore
) {
    suspend fun uploadFile(content: ByteReadChannel, contentType: ContentType): String {
        val fileInfoForScan = virusScanner.prepareForScan(content, contentType)
        fileStore.findFile(fileInfoForScan.md5Hash)?.let {
            return it.name
        }
        val scannedContent = virusScanner.scan(fileInfoForScan.name)

        return try {
            // TODO update metadata to allow uploader to download?
            val fileInfo = fileStore.save(scannedContent, contentType, fileInfoForScan.md5Hash)
            fileInfo.name
        } catch (ex: FileStore.DuplicatedFileException) {
            fileInfoForScan.md5Hash
        }
    }

    suspend fun downloadFile(fileName: String, range: String?): HttpResponse {
        // TODO verify ACL fileMeta
        return fileStore.download(fileName, range)
    }

    // TODO create methods to update file ACL
}
