package domain

import infrastructure.HttpVirusScanner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.Clock

class FileSharingServiceTest : StringSpec({

    "If the file has malicious content, it should throw an exception" {
        val scanner = mockk<VirusScanner>()
        val fileStore = mockk<FileStore>()
        val service = FileSharingService(scanner, fileStore)

        val fileName = "malicious file"
        coEvery { scanner.prepareForScan(any(), any(), any()) } returns FileInfo(
            fileName,
            "contentType",
            "hashHash",
            Clock.System.now()
        )
        coEvery { scanner.scan(fileName) } throws HttpVirusScanner.FileVirusException(fileName)

        shouldThrow<HttpVirusScanner.FileVirusException> {
            service.uploadFile(ByteReadChannel("Malicious content"), ContentType.parse("text/plain"))
        }
    }
})
