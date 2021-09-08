package e2e

import e2e.extensions.GithubJson
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object Mocks {
    val github = MockServer().apply {
        matchRequest(Github::matcher, Github::dispatcher)
    }
    val hops = MockServer().apply {
        matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher)
    }

    object Github {
        fun matcher(req: RecordedRequest): Boolean = when (req.path) {
            "/repos/navikt/helseopplysninger/dispatches" -> req.method == "POST"
            else -> false.also { error("Unhandled github request") }
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun dispatcher(req: RecordedRequest): MockResponse {
            req.headers[HttpHeaders.Accept] shouldBe GithubJson.toString()
            req.headers[HttpHeaders.ContentType] shouldBe ContentType.Application.Json.toString()
            Json.decodeFromString<Results>(req.body.readUtf8()).let { results ->
                results.eventType shouldBe "e2e"
                results.clientPayload.failedTests shouldHaveSize 0
            }

            return MockResponse()
                .setResponseCode(HttpStatusCode.NoContent.value)
                .addHeader("Content-Type", "text/plain")
                .setBody("")
        }
    }

    object Internal {
        fun livenessMatcher(req: RecordedRequest): Boolean = when (req.path) {
            "/isAlive" -> req.method == "GET"
            else -> false.also { error("Unhandled hops liveness request") }
        }

        fun livenessDispatcher(req: RecordedRequest): MockResponse = MockResponse()
            .setResponseCode(HttpStatusCode.OK.value)
            .setBody("live")
    }
}
