package no.nav.helse.hops

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class HapiMockDispatcher : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!request.headers.any { x -> x.first == HttpHeaders.Authorization && x.second.startsWith("Bearer ey") })
            return MockResponse().setResponseCode(HttpStatusCode.Unauthorized.value)

        if (request.method == HttpMethod.Get.value) {
            val body = when (Url(request.path!!)) {
                Url("/tasks") -> TaskTestData.bundleWithSingleEntity // Skal vel returnere et array av tasks
                else -> ""
            }

            return MockResponse().setBody(body).setHeader(HttpHeaders.ContentType, "application/fhir+json")
        }

        return MockResponse().setResponseCode(HttpStatusCode.NotImplemented.value)
    }
}
