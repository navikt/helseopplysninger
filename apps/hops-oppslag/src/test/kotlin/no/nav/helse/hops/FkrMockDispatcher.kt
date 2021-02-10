package no.nav.helse.hops

import io.ktor.http.*
import okhttp3.mockwebserver.*

class FkrMockDispatcher: Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!request.headers.any { x -> x.first == HttpHeaders.Authorization && x.second.startsWith("Bearer") }) {
            return MockResponse().apply { setResponseCode(HttpStatusCode.Unauthorized.value) }
        }

        if (request.headers.contains(Pair(HttpHeaders.Accept, ContentType.Application.Json.toString()))) {
            val path = Url(request.path!!)

            if ((request.method == HttpMethod.Get.value) and
                (path == Url("/Practitioner?identifier=urn:oid:2.16.578.1.12.4.1.4.4|9111492"))) {
                return MockResponse().apply {
                    setBody(PractitionerTestData.bundleWithSingleEntity)
                    setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }
        }

        return MockResponse().apply { setResponseCode(HttpStatusCode.NotImplemented.value) }
    }
}