package fileshare

import Helpers
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.ktor.haveHeader
import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.should
import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import okhttp3.mockwebserver.MockResponse
import withFileshareTestApp

class DownloadFileTest : FeatureSpec({
    feature("GET /files/{filename}") {
        scenario("with existing file returns the file") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = Helpers.oAuthMock.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "Content"
                }
            }
        }

        scenario("with non existing file returns 404 NOT FONUD") {
            Helpers.gcsMockServer.matchRequest(
                { request -> request.path?.contains("nonexistentfile") ?: false },
                {
                    MockResponse()
                        .setResponseCode(HttpStatusCode.NotFound.value)
                }
            )

            withFileshareTestApp {
                with(
                    shouldThrow<ClientRequestException> {
                        handleRequest(HttpMethod.Get, "/files/nonexistentfile") {
                            val token = Helpers.oAuthMock.issueToken()
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
    feature("POST /files") {
        scenario("happy path") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = Helpers.oAuthMock.issueToken()
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
    }
})
