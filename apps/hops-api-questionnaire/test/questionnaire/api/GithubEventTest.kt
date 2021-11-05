package questionnaire.api

import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.junit.Assert.assertNull
import org.junit.jupiter.api.Test
import questionnaire.GithubMock
import questionnaire.resourceFile
import questionnaire.store.QuestionnaireStore
import questionnaire.withTestApp

class GithubEventTest {

    @Test
    fun `ping event returns 204`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Post, "/github/event") {
                        addHeader("Accept", "*/*")
                        addHeader("content-type", "application/json")
                        addHeader("X-GitHub-Event", "ping")
                        setBody(resourceFile("/github/ping.json"))
                    }
                ) {
                    response shouldHaveStatus 204
                }
            }
        }
    }

    @Test
    fun `release event returns 202 and will fetch and store lastest release`() {
        GithubMock().use {
            withTestApp(it) {

                assertNull(QuestionnaireStore.get("hjelpestonad-1.0.1"))

                with(
                    handleRequest(HttpMethod.Post, "/github/event") {
                        addHeader("Accept", "*/*")
                        addHeader("content-type", "application/json")
                        addHeader("X-GitHub-Event", "release")
                        setBody(resourceFile("/github/published.json"))
                    }
                ) {
                    response shouldHaveStatus 202
                    QuestionnaireStore.get("hjelpestonad-1.0.1") shouldNotBe null
                }
            }
        }
    }

    @Test
    fun `other events returns 422`() {
        GithubMock().use {
            withTestApp(it) {
                with(
                    handleRequest(HttpMethod.Post, "/github/event") {
                        addHeader("Accept", "*/*")
                        addHeader("content-type", "application/json")
                        addHeader("X-GitHub-Event", "issue")
                        setBody("""{"dummy":"json"}""")
                    }
                ) {
                    response shouldHaveStatus 422
                }
            }
        }
    }
}
