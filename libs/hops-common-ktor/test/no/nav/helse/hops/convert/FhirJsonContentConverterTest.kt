package no.nav.helse.hops.convert

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveHeader
import io.kotest.assertions.ktor.shouldHaveStatus
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
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
            addHeader(HttpHeaders.ContentType, "application/fhir+json; fhirVersion=4.0")
            setBody(patientJson)
        }.response.let { response ->
            response shouldHaveStatus HttpStatusCode.OK
            response shouldHaveContent patientJson
            response.shouldHaveHeader(
                name = HttpHeaders.ContentType,
                value = ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).contentType
            )
        }
    }
}
