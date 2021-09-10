package e2e

import io.ktor.http.HttpStatusCode
import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object HopsMocks {
    val api = MockServer().apply {
        matchRequest(matchActuatorLive(), dispatchLiveSuccess())
    }

    val eventreplay = MockServer().apply {
        matchRequest(matchActuatorLive(), dispatchLiveSuccess())
    }

    val eventsink = MockServer().apply {
        matchRequest(matchActuatorLive(), dispatchLiveSuccess())
    }

    val eventstore = MockServer().apply {
        matchRequest(matchActuatorLive(), dispatchLiveSuccess())
    }

    val fileshare = MockServer().apply {
        matchRequest(matchActuatorLive(), dispatchLiveSuccess())
    }

    fun matchActuatorLive() = { req: RecordedRequest ->
        req.method == "GET" && req.path?.startsWith("/isAlive") ?: false
    }

    private fun dispatchLiveSuccess() = { _: RecordedRequest ->
        MockResponse().setResponseCode(HttpStatusCode.OK.value).setBody("live")
    }
}
