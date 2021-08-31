package fileshare

import Handlers
import io.kotest.assertions.ktor.haveHeader
import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.ServerResponseException
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import java.net.URL
import java.util.LinkedList
import no.nav.security.mock.oauth2.MockOAuth2Server
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

fun <R> withFileshareTestApp(
    http: HttpClient = Handlers.happyPathHandler,
    test: TestApplicationEngine.() -> R
): R {
    return withTestApplication(
        {
            config()
            main(http)
        },
        test
    )
}

class DownloadFileTest : FeatureSpec({
    feature("GET /files/{filename}") {
        scenario("with existing file returns the file") {
            withFileshareTestApp(Handlers.happyPathHandler) {
                with(
                    handleRequest(HttpMethod.Get, "/files/testfile") {
                        val token = oAuthMock.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response shouldHaveContent "downloaded file"
                }
            }
        }

        scenario("with nonexisting file returns 404 NOT FONUD") {
            gcsMockServer.matchRequest(
                { request -> request.path?.contains("nonexistentfile") ?: false },
                {
                    MockResponse()
                        .setResponseCode(HttpStatusCode.NotFound.value)
                }
            )

            withFileshareTestApp(Handlers.happyPathHandler) {
                with(
                    shouldThrow<ServerResponseException> {
                        handleRequest(HttpMethod.Get, "/files/nonexistentfile") {
                            val token = oAuthMock.issueToken()
                            addHeader("Authorization", "Bearer ${token.serialize()}")
                        }
                    }
                ) {
                    message shouldBe "Server error(http://localhost:4443/storage/v1/b/hops/o/nonexistentfile?alt=media: 500 Internal Server Error. Text: \"bad stuff happened\""
                }
            }
        }
    }
})

class UploadFileTest : FeatureSpec({
    feature("POST /files") {
        xscenario("happy path") {
            withFileshareTestApp {
                with(
                    handleRequest(HttpMethod.Post, "/files") {
                        val token = oAuthMock.issueToken()
                        addHeader("Authorization", "Bearer ${token.serialize()}")
                        addHeader("Content-Type", "plain/txt")
                        setBody("new fantastic content")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.Created
                    response should haveHeader("Location", "")
                }
            }
        }
    }
})

internal class KotestSetup : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestListener()
}

internal class KotestListener : ProjectListener {
    override suspend fun beforeProject() {
//        gcsMockServer.start()
//        gcpMetadataMockServer.start()
//        virusScannerMockServer.start()
        startOAuth()
    }

    override suspend fun afterProject() {
        stopOAuth()
//        gcsMockServer.shutdown()
//        gcpMetadataMockServer.shutdown()
//        virusScannerMockServer.shutdown()
    }
}

val oAuthMock = MockOAuth2Server()
val gcsMockServer = MockServer().apply {
    matchRequest(
        { request -> request.method == "POST" && request.path?.startsWith("/upload/storage/v1/b") ?: false },
        {
            MockResponse()
                .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                .setBody(
                    """
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
                )
        }
    )
    matchRequest(
        { request -> request.method == "GET" && request.path?.startsWith("/storage/v1/b") ?: false },
        {
            MockResponse()
                .setHeader(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                .setBody("Content")
        }
    )
}
val gcpMetadataMockServer = MockServer().apply {
    anyRequest {
        MockResponse()
            .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            .setBody("""{"access_token":"ya29.c.KtECDwgryW8_sV7xqm8sc4yNAyQQuT8wXuN3j8wegpTyoNt5HPMWJUfFq5ubF2EWadqHPYFEwybxtmzsNwAxFbYWnwiJ-u1S6F6J7Sdi4x12YBaA07QbsFX_44jAdDixz5t3PocIdu5fMJl2FtP4IyfcVSAz1WE50FMpVJM0JWq1JPuNE2FhWcd4uHDIMdXWEMpblH87Zp9OXlMo8urA0OJJCm-LQ1RAmkCNteOsfgTL7YLkdaxKq9eFz-SlKSo-fd8iBcmQc8aGGXwZoVMcfvBms__f-IZW5Gf4Wmgm2LpokidHfyMq0C--cRydSmKmiq15ltgDHgumCHRp96xqBVjC7m1eDnCYJHJ6iC8DOHD2eorxWWC-AP4UqOF_rlhyTHHx2RI3XRtg9crpc6_YhYMykUnSoHffh8rRkQ7cWTxU76ibU-PA-7GlCUyhFb_VUmTLqg","expires_in":3599,"token_type":"Bearer"}""")
    }
}
val virusScannerMockServer = MockServer().apply {
    anyRequest {
        MockResponse()
            .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            .setBody("""[{"result":"OK"}]""")
    }
}

internal fun startOAuth() = with(oAuthMock, MockOAuth2Server::start)
internal fun stopOAuth() = with(oAuthMock, MockOAuth2Server::shutdown)

private fun Application.config() = (environment.config as MapApplicationConfig).apply {
    put("no.nav.security.jwt.issuers.size", "1")
    put("no.nav.security.jwt.issuers.0.issuer_name", "default")
    put("no.nav.security.jwt.issuers.0.discoveryurl", "${oAuthMock.wellKnownUrl("default")}")
    put("no.nav.security.jwt.issuers.0.accepted_audience", "default")
    put("security.scopes.publish", "/test-publish")
    put("security.scopes.subscribe", "/test-subscribe")
    put("fileStore.baseUrl", gcsMockServer.getBaseUrl().toString())
    put("fileStore.bucketName", "hops")
    put("fileStore.requiresAuth", "true")
    put("fileStore.virusScanningEnabled", "true")
    put("fileStore.unScannedBucketName", "hops-unscanned")
    put(
        "fileStore.tokenFetchUrl",
        "${gcpMetadataMockServer.getBaseUrl()}/computeMetadata/v1/instance/service-accounts/default/token"
    )
    put("fileStore.virusScannerUrl", "${virusScannerMockServer.getBaseUrl()}/scan")
}

class MockServer() {
    val mockWebServer = MockWebServer()
    private val dispatchChain = LinkedList<MatchAndDispatch>()

    init {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                dispatchChain.forEach {
                    if (it.matcher.invoke(request)) {
                        return it.dispatcher.invoke(request)
                    }
                }
                return defaultDispatcher(request)
            }
        }
    }

    var defaultDispatcher: (RecordedRequest) -> MockResponse = {
        MockResponse()
            .setResponseCode(HttpStatusCode.NotFound.value)
    }

    fun start() = mockWebServer.start()
    fun shutdown() = mockWebServer.shutdown()

    private class MatchAndDispatch(
        val matcher: (RecordedRequest) -> Boolean,
        val dispatcher: (RecordedRequest) -> MockResponse
    )

    fun getBaseUrl(): URL = URL("http://localhost:8081")
//    fun getBaseUrl(): URL = mockWebServer.url("/").toUrl()

    fun matchRequest(
        matcher: (RecordedRequest) -> Boolean,
        dispatcher: (RecordedRequest) -> MockResponse
    ) {
        dispatchChain.addFirst(MatchAndDispatch(matcher, dispatcher))
    }

    fun anyRequest(dispatcher: (RecordedRequest) -> MockResponse) {
        dispatchChain.addFirst(MatchAndDispatch({ true }, dispatcher))
    }
}
