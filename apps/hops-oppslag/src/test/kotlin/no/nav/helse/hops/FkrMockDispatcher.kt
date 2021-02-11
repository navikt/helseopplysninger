package no.nav.helse.hops

import io.ktor.http.*
import okhttp3.mockwebserver.*

class FkrMockDispatcher: Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!request.headers.any { x -> x.first == HttpHeaders.Authorization && x.second.startsWith("Bearer ey") })
            return MockResponse().setResponseCode(HttpStatusCode.Unauthorized.value)

        if (request.method == HttpMethod.Get.value) {
            val body = when (Url(request.path!!)) {
                Url("/Practitioner?identifier=urn:oid:2.16.578.1.12.4.1.4.4|9111492") -> PractitionerTestData.bundleWithSingleEntity
                else -> ""
            }

            return MockResponse().setBody(body).setHeader(HttpHeaders.ContentType, "application/fhir+json")
        }

        return MockResponse().setResponseCode(HttpStatusCode.NotImplemented.value)
    }
}