package fileshare

import MockServers
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.ktor.haveHeader
import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.should
import io.ktor.client.features.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import okhttp3.mockwebserver.MockResponse
import withFileshareTestApp

class DownloadFileTest : FeatureSpec({
    feature("Download authorization") {
        scenario("Token by issuer that requires scope claims has scopes") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueToken(issuerId = "with-scopes", claims = mapOf("scope" to "nav:helse:helseopplysninger.read"))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                }
            }
        }

        scenario("Issuer that requires scope claims does not have the required scope") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueToken(issuerId = "with-scopes")
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Unauthorized
                }
            }
        }

        scenario("Issuer that does not require scope claims can download") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                }
            }
        }
    }

    feature("GET /files/{filename}") {
        scenario("with existing file returns the file") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = MockServers.oAuth.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "Content"
                }
            }
        }

        scenario("with non existing file returns 404 NOT FONUD") {
            withFileshareTestApp {
                MockServers.gcs.matchRequest(
                    { request -> request.path?.contains("nonexistentfile") ?: false },
                    {
                        MockResponse()
                            .setResponseCode(HttpStatusCode.NotFound.value)
                    }
                )
                with(
                    shouldThrow<ClientRequestException> {
                        handleRequest(HttpMethod.Get, "/files/nonexistentfile") {
                            val token = MockServers.oAuth.issueToken()
                            addHeader("Authorization", "Bearer ${token.serialize()}")
                        }
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.NotFound
                }
            }
        }
    }
})

class UploadFileTest : FeatureSpec({
    feature("Upload authorization") {
        scenario("Token by issuer that requires scope claims has scopes") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueToken(issuerId = "with-scopes", claims = mapOf("scope" to "nav:helse:helseopplysninger.write"))
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Created
                }
            }
        }

        scenario("Token by issuer that requires scope claims does not have the required scope") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueToken(issuerId = "with-scopes")
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Unauthorized
                }
            }
        }

        scenario("Token by issuer that does not require scope claims can upload") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Created
                }
            }
        }
    }

    feature("POST /files") {
        scenario("happy path") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = MockServers.oAuth.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Created
                    response should haveHeader("Location", "http://localhost/files/file-name")
                }
            }
        }

        scenario("Uploading a file that contains malicious content it should give back an error") {
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
                        val token = MockServers.oAuth.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("malicious content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                }
            }
        }
    }
})
