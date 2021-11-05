package questionnaire.api

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.helse.hops.content.JsonFhirR4
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Questionnaire
import org.junit.jupiter.api.Test
import questionnaire.GithubMock
import questionnaire.bodyAsBundle
import questionnaire.resourceFile
import questionnaire.withTestApp

class QuestionnaireSearchTest {

    private val fhirContentType = ContentType.Application.JsonFhirR4.withCharset(Charsets.UTF_8)

    @Test
    fun `can search for grunnstonad by url`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(
                        method = HttpMethod.Get,
                        uri = "/4.0/questionnaire?url=http://fhir.nav.no/Questionnaire/grunnstonad"
                    )
                ) {
                    response.contentType() shouldBe fhirContentType
                    response shouldHaveStatus 200

                    val bundle: Bundle = response.bodyAsBundle()
                    val actual: Questionnaire = bundle.entry.single().resource as Questionnaire
                    val expected = resourceFile<Questionnaire>("/questionnaires/grunnstonad-1.0.0.json")

                    assertSoftly {
                        actual.url shouldBe expected.url
                        actual.idElement.idPart shouldBe "${expected.idElement.idPart}-${expected.version}"
                        actual.version shouldBe expected.version
                    }
                }
            }
        }
    }

    @Test
    fun `can search for grunnstonad by versioned url`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(
                        method = HttpMethod.Get,
                        uri = "/4.0/questionnaire?url=http://fhir.nav.no/Questionnaire/grunnstonad|1.0.0"
                    )
                ) {
                    response.contentType() shouldBe fhirContentType
                    response shouldHaveStatus 200

                    val bundle: Bundle = response.bodyAsBundle()
                    val actual: Questionnaire = bundle.entry.single().resource as Questionnaire
                    val expected = resourceFile<Questionnaire>("/questionnaires/grunnstonad-1.0.0.json")

                    assertSoftly {
                        actual.url shouldBe expected.url
                        actual.idElement.idPart shouldBe "${expected.idElement.idPart}-${expected.version}"
                        actual.version shouldBe expected.version
                    }
                }
            }
        }
    }

    @Test
    fun `can search for hjelpestonad with POST endpoint by url`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(method = HttpMethod.Post, uri = "/4.0/questionnaire/_search") {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setBody("url=http://fhir.nav.no/Questionnaire/hjelpestonad")
                    }
                ) {
                    response.contentType() shouldBe fhirContentType
                    response shouldHaveStatus 200

                    val bundle: Bundle = response.bodyAsBundle()
                    val actual: Questionnaire = bundle.entry.single().resource as Questionnaire
                    val expected = resourceFile<Questionnaire>("/questionnaires/hjelpestonad-1.0.0.json")

                    assertSoftly {
                        actual.url shouldBe expected.url
                        actual.idElement.idPart shouldBe "${expected.idElement.idPart}-${expected.version}"
                        actual.version shouldBe expected.version
                    }
                }
            }
        }
    }

    @Test
    fun `can search for hjelpestonad with POST endpoint by url and version`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(method = HttpMethod.Post, uri = "/4.0/questionnaire/_search") {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setBody("url=http://fhir.nav.no/Questionnaire/hjelpestonad&version=1.0.0")
                    }
                ) {
                    response.contentType() shouldBe fhirContentType
                    response shouldHaveStatus 200

                    val bundle: Bundle = response.bodyAsBundle()
                    val actual: Questionnaire = bundle.entry.single().resource as Questionnaire
                    val expected = resourceFile<Questionnaire>("/questionnaires/hjelpestonad-1.0.0.json")

                    assertSoftly {
                        actual.url shouldBe expected.url
                        actual.idElement.idPart shouldBe "${expected.idElement.idPart}-${expected.version}"
                        actual.version shouldBe expected.version
                    }
                }
            }
        }
    }

    @Test
    fun `search returns 415 when searching with wrong content type with POST endpoint`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(method = HttpMethod.Post, uri = "/4.0/questionnaire/_search") {
                        addHeader("Content-Type", "application/fhir+json")
                        setBody("url=http://fhir.nav.no/Questionnaire/hjelpestonad")
                    }
                ) {
                    response shouldHaveStatus 415
                }
            }
        }
    }
}
