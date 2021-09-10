package e2e

import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object HopsMocks {
    val api = MockServer().apply {
        matchRequest(isAlive(), dispatchLiveSuccess())
    }

    val eventreplay = MockServer().apply {
        matchRequest(isAlive(), dispatchLiveSuccess())
    }

    val eventsink = MockServer().apply {
        matchRequest(isAlive(), dispatchLiveSuccess())
    }

    val eventstore = MockServer().apply {
        matchRequest(isAlive(), dispatchLiveSuccess())
    }

    val fileshare = MockServer().apply {
        matchRequest(isAlive(), dispatchLiveSuccess())
    }

    fun isAlive() = { req: RecordedRequest ->
        req.method == "GET" && req.path?.startsWith("/isAlive") ?: false
    }

    private fun dispatchLiveSuccess() = { _: RecordedRequest ->
        MockResponse().setResponseCode(200).setBody("live")
    }
}
