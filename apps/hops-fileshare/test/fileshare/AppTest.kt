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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server

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
    override suspend fun beforeProject() = startOAuth()
    override suspend fun afterProject() = stopOAuth()
}

val oAuthMock = MockOAuth2Server()

internal fun startOAuth() = with(oAuthMock, MockOAuth2Server::start)
internal fun stopOAuth() = with(oAuthMock, MockOAuth2Server::shutdown)

private fun Application.config() = (environment.config as MapApplicationConfig).apply {
    put("no.nav.security.jwt.issuers.size", "1")
    put("no.nav.security.jwt.issuers.0.issuer_name", "default")
    put("no.nav.security.jwt.issuers.0.discoveryurl", "${oAuthMock.wellKnownUrl("default")}")
    put("no.nav.security.jwt.issuers.0.accepted_audience", "default")
    put("security.scopes.publish", "/test-publish")
    put("security.scopes.subscribe", "/test-subscribe")
}
