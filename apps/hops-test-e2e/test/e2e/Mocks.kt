package e2e

import io.ktor.http.HttpStatusCode
import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object Mocks {
    val github = MockServer().apply {
        matchRequest(Github::matcher, Github::dispatcher)
    }
    val internal = MockServer().apply {
        matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher)
    }

    object Github {
        fun matcher(req: RecordedRequest): Boolean = when (req.path) {
            "/repos/navikt/helseopplysninger/dispatches" -> req.method == "POST"
            else -> false.also { error("Unhandled github request") }
        }

        fun dispatcher(req: RecordedRequest): MockResponse = MockResponse()
            .setResponseCode(HttpStatusCode.NoContent.value)
            .setBody("")
    }

    object Internal {
        private val livenessProbes = listOf(
            "http://hops-api/isAlive",
            "http://hops-eventreplaykafka/isAlive",
            "http://hops-eventsinkkafka/isAlive",
            "http://hops-eventstore/isAlive",
            "http://hops-fileshare/isAlive",
            "http://hops-test-external/isAlive",
        )

        fun livenessMatcher(req: RecordedRequest): Boolean = when (req.requestUrl?.encodedPath) {
            in livenessProbes -> req.method == "GET"
            else -> false.also { error("Unhandled internal liveness probe") }
        }

        fun livenessDispatcher(req: RecordedRequest): MockResponse = MockResponse()
            .setResponseCode(HttpStatusCode.OK.value)
            .setBody("live")
    }
}
