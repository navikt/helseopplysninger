package fileshare

import io.ktor.client.features.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlin.test.assertEquals
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockServers.Setup::class)
class DownloadFileTest {
    @Nested
    inner class DownloadAuthorization {
        @Test
        fun `Token by issuer that requires scope claims has scopes`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.READ))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }

        @Test
        fun `Issuer that requires scope claims does not have the required scope`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf())
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

        @Test
        fun `Issuer that does not require scope claims can download`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueAzureToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /files/{filename}")
    inner class GetFiles {
        @Test
        fun `with existing file returns the file`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueAzureToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Content", response.content)
                }
            }
        }

        @Test
        fun `with non existing file returns 404 NOT FONUD`() {
            withFileshareTestApp {
                MockServers.gcs.matchRequest(
                    { request -> request.path?.contains("nonexistentfile") ?: false },
                    {
                        MockResponse()
                            .setResponseCode(HttpStatusCode.NotFound.value)
                    }
                )
                with(
                    assertThrows<ClientRequestException> {
                        handleRequest(HttpMethod.Get, "/files/nonexistentfile") {
                            val token = MockServers.oAuth.issueAzureToken()
                            addHeader("Authorization", "Bearer ${token.serialize()}")
                        }
                    }
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status)
                }
            }
        }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockServers.Setup::class)
class UploadFileTest {
    @Nested
    inner class UploadAuthorization {
        @Test
        fun `Token by issuer that requires scope claims has scopes`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.WRITE))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }

        @Test
        fun `Token by issuer that requires scope claims does not have the required scope`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueMaskinportenToken(scopes = setOf(MaskinportenScopes.READ))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

        @Test
        fun `Token by issuer that does not require scope claims can upload`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueAzureToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }
    }

    @Nested
    @DisplayName("POST /files}")
    inner class PublishFiles {
        @Test
        fun `happy path`() {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueAzureToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    assertEquals(HttpStatusCode.Created, response.status())
                    assertEquals("http://localhost/files/file-name", response.headers["Location"])
                }
            }
        }

        @Test
        fun `Uploading a file that contains malicious content it should give back an error`() {
            withFileshareTestApp {
                MockServers.gcs.matchRequest(
                    { request -> request.body.readUtf8() == "malicious content" },
                    {
                        MockResponse()
                            .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            .setBody(MockServers.gcsFileInfoResponse.replace("file-name", "malicious-file-name"))
                    }
                )
                MockServers.gcs.matchRequest(
                    { request -> request.path?.contains("malicious-file-name") ?: false },
                    {
                        MockResponse()
                            .setBody("malicious content")
                    }
                )
                MockServers.virusScanner.matchRequest(
                    { request -> request.body.readUtf8() == "malicious content" },
                    {
                        MockResponse()
                            .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            .setBody("""[{"result":"FOUND"}]""")
                    }
                )
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueAzureToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("malicious content")
                    }
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }
        }
    }
}
