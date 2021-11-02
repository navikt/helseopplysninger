package questionnaire.api

import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import no.nav.helse.hops.content.JsonFhirR4
import org.junit.jupiter.api.Test
import questionnaire.GithubMock
import questionnaire.withTestApp

class QuestionnaireReadTest {

    private val fhirContentType = ContentType.Application.JsonFhirR4.withCharset(Charsets.UTF_8)

    @Test
    fun `hjelpestonad with configured ContentNegotiation - 200 OK`() {
        GithubMock().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, "/4.0/questionnaire/hjelpestonad-1.0.0")) {
                    response shouldHaveStatus 200
                    response.contentType() shouldBe fhirContentType
                    response.content shouldNotBe null
                }
            }
        }
    }

    @Test
    fun `hjelpestonad with Accept=fhir+json - 200 OK`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Get, "/4.0/questionnaire/hjelpestonad-1.0.0") {
                        addHeader("Accept", "application/fhir+json")
                    }
                ) {
                    response shouldHaveStatus 200
                    response.contentType() shouldBe fhirContentType
                    response.content shouldNotBe null
                }
            }
        }
    }

    @Test
    fun `hjelpestonad with Accept=fhir+json versioned - 200 OK`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Get, "/4.0/questionnaire/hjelpestonad-1.0.0") {
                        addHeader("Accept", "application/fhir+json; fhirVersion=4.0")
                    }
                ) {
                    response shouldHaveStatus 200
                    response.contentType() shouldBe fhirContentType
                    response.content shouldNotBe null
                }
            }
        }
    }

    @Test
    fun `hjelpestonad with Accept=fhir - 406 Not Acceptable`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Get, "/4.0/questionnaire/hjelpestonad-1.0.0") {
                        addHeader("Accept", "application/fhir")
                    }
                ) {
                    response shouldHaveStatus 406
                }
            }
        }
    }

    @Test
    fun `hjelpestonad with Accept=json - 406 Not Acceptable`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Get, "/4.0/questionnaire/hjelpestonad-1.0.0") {
                        addHeader("Accept", "application/json")
                    }
                ) {
                    response shouldHaveStatus 406
                }
            }
        }
    }
}
