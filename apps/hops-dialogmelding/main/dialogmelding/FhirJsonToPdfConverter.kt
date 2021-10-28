package dialogmelding

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class FhirJsonToPdfConverter(
    private val config: Config.Endpoint,
    private val client: HttpClient
) {
    suspend fun convertToPdf(json: ByteArray, contentType: ContentType) =
        client.post<ByteArray>("${config.baseUrl}/\$convert") {
            contentType(contentType)
            accept(ContentType.Application.Pdf)
            body = json
        }
}
