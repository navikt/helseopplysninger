package no.nav.helse.hops

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*

object FkrMock {
    const val HOST = "fkr-test.no"

    val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                if ((request.method == HttpMethod.Get) and
                    (request.url.encodedPath == "Practitioner") and
                    request.headers.contains(HttpHeaders.Accept, ContentType.Application.Json.toString())) {
                    val responseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    respond(PractitionerTestData.bundleWithSingleEntity, headers = responseHeaders)
                }
                else respondError(HttpStatusCode.NotImplemented)
            }
        }
        defaultRequest {
            host = HOST
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
    }
}