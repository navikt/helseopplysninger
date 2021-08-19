import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes

internal fun createEventStoreMockClient() =
    HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.fullPath) {
                    "/fhir/4.0/Bundle" -> stubRequest(request)
                    "/fhir/4.0/\$process-message" -> stubRequest(request)
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

private val headers = headersOf(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString())

private fun MockRequestHandleScope.stubRequest(request: HttpRequestData): HttpResponseData =
    when (request.method) {
        HttpMethod.Get -> respond(headers = headers, content = """{"resourceType": "Bundle"}""")
        HttpMethod.Post -> respond(headers = headers, content = "")
        else -> error("Unhandled ${request.method}")
    }
