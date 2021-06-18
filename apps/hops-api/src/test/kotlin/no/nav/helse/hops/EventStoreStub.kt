package no.nav.helse.hops

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes

fun createEventStoreMockClient() =
    HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                if (request.url.fullPath.contains("/Bundle")) {
                    val ct = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()
                    val responseHeaders = headersOf("Content-Type" to listOf(ct))
                    respond("""{"resourceType": "Bundle"}""", headers = responseHeaders)
                } else {
                    error("Unhandled ${request.url}")
                }
            }
        }
    }
