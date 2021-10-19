package fileshare.infrastructure

import fileshare.domain.FileStore
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GCPHttpTransportTest {

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
    fun jsonMockClient(block: HttpClientConfig<MockEngineConfig>.() -> Unit) = HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json { ignoreUnknownKeys = true })
        }
        block()
    }

    @Test
    fun `Can deserialize response`() {
        val client = jsonMockClient {
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

        val (name, contentType, _, created) = runBlocking {
            transport.upload(
                "test",
                ContentType.parse("image/png"),
                ByteReadChannel("content"),
                "file-name"
            )
        }

        assertEquals("file-name", name)
        assertEquals("image/png", contentType)
        assertEquals(Instant.parse("2021-08-24T07:57:10.91259Z"), created)
    }

    @Test
    fun `Should decode md5 hash to hex`() {
        val client = jsonMockClient {
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

        val (_, _, md5Hash, _) = runBlocking {
            transport.upload(
                "test",
                ContentType.parse("image/png"),
                ByteReadChannel("content"),
                "file-name"
            )
        }

        assertEquals("a8a60378d728a68d4115263278a8e46f", md5Hash)
    }

    @Test
    fun `Should throw exception when file already exists`() {
        val client = jsonMockClient {
            engine {
                addHandler {
                    respond(
                        content = "{}",
                        headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())),
                        status = HttpStatusCode.PreconditionFailed
                    )
                }
            }
        }
        val transport = GCPHttpTransport(client, baseConfig)
        assertThrows<FileStore.DuplicatedFileException> {
            runBlocking {
                transport.upload("bucket", ContentType.parse("plain/txt"), ByteReadChannel("test"), "file-name")
            }
        }
    }

    @Test
    fun `Should send Google meta-data header to obtain tokens`() {

        var receivedHeaders = Headers.Empty
        val client = jsonMockClient {
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

        runBlocking {
            transport.download("test", "file-name")
        }

        assertTrue(receivedHeaders.contains("Metadata-Flavor", "Google"))
    }

    @Test
    fun `!Can retrieve access token`() {

        val client = jsonMockClient {
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
}
