package no.nav.helse.hops.convert

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.testing.withTestApplication
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Test
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FhirJsonContentConverterTest {
    @Test
    fun testConvertOfFhirResource() = withTestApplication {
        application.apply {
            install(ContentNegotiation) {
                register(ContentTypes.fhirJson, FhirJsonContentConverter())
            }
            routing {
                post("/") {
                    val patient = call.receive<Patient>()
                    call.respond(patient)
                }
            }
        }

        val patientJson = """{"resourceType":"Patient","id":"hello-world","gender":"female"}"""

        handleRequest(HttpMethod.Post, "/") {
            addHeader("Content-Type", "application/fhir+json")
            setBody(patientJson)
        }.response.let { response ->
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content)
            assertEquals(patientJson, response.content)
            val contentTypeText = assertNotNull(response.headers[HttpHeaders.ContentType])
            assertEquals(ContentTypes.fhirJson.withCharset(Charsets.UTF_8), ContentType.parse(contentTypeText))
        }

    }
}
