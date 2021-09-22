package no.nav.helse.hops.test

import io.ktor.http.HttpStatusCode
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.LinkedList

class MockServer {
    private val mockWebServer = MockWebServer()
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
        MockResponse().setResponseCode(HttpStatusCode.NotFound.value)
    }

    fun start() = mockWebServer.start()
    fun shutdown() = mockWebServer.shutdown()

    private class MatchAndDispatch(
        val matcher: (RecordedRequest) -> Boolean,
        val dispatcher: (RecordedRequest) -> MockResponse,
    )

    fun getBaseUrl(): String = mockWebServer.url("/").toString().removeSuffix("/")

    fun matchRequest(
        matcher: (RecordedRequest) -> Boolean,
        dispatcher: (RecordedRequest) -> MockResponse,
    ) {
        dispatchChain.addFirst(MatchAndDispatch(matcher, dispatcher))
    }

    fun anyRequest(dispatcher: (RecordedRequest) -> MockResponse) {
        dispatchChain.addFirst(MatchAndDispatch({ true }, dispatcher))
    }
}