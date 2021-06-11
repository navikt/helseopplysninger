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
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private const val patientJson = """{
  "resourceType": "Patient",
  "id": "hello-world",
  "gender": "female"
}"""

class FhirJsonContentConverterTest {
    @Test
    fun testConvertOfFhirResource() = withTestApplication {
        application.apply {
            install(ContentNegotiation) {
                register(ContentTypes.fhirJson, FhirR4JsonContentConverter())
            }
            routing {
                post("/") {
                    val patient = call.receive<Patient>()
                    call.respond(patient)
                }
            }
        }

        handleRequest(HttpMethod.Post, "/") {
            addHeader("Content-Type", "application/fhir+json; fhirVersion=4.0")
            setBody(patientJson)
        }.response.let { response ->
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content)
            assertEquals(patientJson, response.content)
            val contentTypeText = assertNotNull(response.headers[HttpHeaders.ContentType])
            assertEquals(ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8), ContentType.parse(contentTypeText))
        }
    }
}
