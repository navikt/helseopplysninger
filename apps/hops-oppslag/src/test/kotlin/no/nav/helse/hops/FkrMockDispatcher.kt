package no.nav.helse.hops

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class FkrMockDispatcher : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!request.headers.any { x -> x.first == HttpHeaders.Authorization && x.second.startsWith("Bearer ey") })
            return MockResponse().setResponseCode(HttpStatusCode.Unauthorized.value)

        if (request.method == HttpMethod.Get.value) {
            val practitioner = loadResource("/fhir/valid-practitioner.json")
            val body = when (Url(request.path!!)) {
                Url("/Practitioner?identifier=urn:oid:2.16.578.1.12.4.1.4.4|9111492") -> practitioner
                else -> ""
            }

            return MockResponse().setBody(body).setHeader(HttpHeaders.ContentType, "application/fhir+json")
        }

        return MockResponse().setResponseCode(HttpStatusCode.NotImplemented.value)
    }

    private fun loadResource(resource: String): String =
        try {
            object {}.javaClass.getResource(resource)!!.readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }
}
