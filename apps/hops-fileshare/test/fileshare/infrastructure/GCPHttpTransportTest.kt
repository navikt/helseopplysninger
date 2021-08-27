package fileshare.infrastructure

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import java.net.URL
import kotlinx.datetime.Instant

class GCPHttpTransportTest : StringSpec({

    val baseConfig = Config.FileStore(
        URL("http://localhost"),
        "",
        false,
        URL("http://localhost/token"),
        false,
        URL("http:localhost/scan"),
        ""
    )

    val exampleResponse = """
        {
            "bucket": "hops",
            "name": "file-name",
            "contentType": "image/png",
            "contentEncoding": "",
            "crc32c": "S8lmMw==",
            "md5Hash": "qKYDeNcopo1BFSYyeKjkbw==",
            "acl": [
                {
                    "entity": "projectOwner",
                    "entityId": "",
                    "role": "OWNER",
                    "domain": "",
                    "email": "",
                    "projectTeam": null
                }
            ],
            "created": "2021-08-24T07:57:10.91259Z",
            "updated": "2021-08-24T07:57:10.912632Z",
            "deleted": "0001-01-01T00:00:00Z"
        }
    """.trimIndent()

    "Can deserialize response" {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = exampleResponse,
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
        }
        val transport = GCPHttpTransport(client, baseConfig)

        val (name, contentType, _, created) = transport.upload(
            "test",
            ContentType.parse("image/png"),
            ByteReadChannel("content"),
            "file-name"
        )

        name shouldBe "file-name"
        contentType shouldBe "image/png"
        created shouldBe Instant.parse("2021-08-24T07:57:10.91259Z")
    }

    "Should decode md5 hash to hex" {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = exampleResponse,
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
        }
        val transport = GCPHttpTransport(client, baseConfig)

        val (_, _, md5Hash, _) = transport.upload(
            "test",
            ContentType.parse("image/png"),
            ByteReadChannel("content"),
            "file-name"
        )

        md5Hash shouldBe "a8a60378d728a68d4115263278a8e46f"
    }

    "Should send Google meta-data header to obtain tokens" {

        var receivedHeaders = Headers.Empty
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    val validResponse = respond(
                        content = """{"access_token":"abc", "expires_in":1, "token_type":"bearer"}""",
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
                    )
                    when (request.url.fullPath) {
                        baseConfig.tokenFetchUrl.path -> {
                            receivedHeaders = request.headers
                            validResponse
                        }
                        else -> validResponse
                    }
                }
            }
        }
        val transport = GCPHttpTransport(client, baseConfig.copy(requiresAuth = true))

        transport.download("test", "file-name")

        receivedHeaders.contains("Metadata-Flavor", "Google") shouldBe true
    }

    "!Can retrieve access token" {

        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = """{"access_token":"abc", "expires_in":1, "token_type":"bearer"}""",
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
        }

        val transport = GCPHttpTransport(client, baseConfig.copy(requiresAuth = true))
    }
})
