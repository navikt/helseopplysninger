package e2e

import io.ktor.http.HttpStatusCode
import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object Mocks {
    val api = MockServer().apply { matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher) }
    val eventreplay = MockServer().apply { matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher) }
    val eventsink = MockServer().apply { matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher) }
    val eventstore = MockServer().apply { matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher) }
    val fileshare = MockServer().apply { matchRequest(Internal::livenessMatcher, Internal::livenessDispatcher) }

    object Internal {
        fun livenessMatcher(req: RecordedRequest): Boolean =
            when (req.path) {
                "/isAlive" -> req.method == "GET"
                else -> false.also { error("Unhandled hops liveness request") }
            }

        fun livenessDispatcher(req: RecordedRequest): MockResponse =
            when (req.requestUrl?.port) {
                fileshare.getPort() -> MockResponse().setResponseCode(HttpStatusCode.ServiceUnavailable.value)
                null -> error("no port found in request for mockserver")
                else -> MockResponse().setResponseCode(HttpStatusCode.OK.value).setBody("live")
            }
    }
}
