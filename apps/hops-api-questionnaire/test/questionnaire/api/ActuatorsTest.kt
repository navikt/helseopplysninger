package questionnaire.api

import io.kotest.assertions.ktor.shouldHaveStatus
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import questionnaire.GithubMock
import questionnaire.Nais
import questionnaire.projectPath
import questionnaire.withTestApp
import questionnaire.yaml

/**
 * Actuators configured in the nais manifest is available
 */
class ActuatorsTest {
    private val nais = yaml<Nais>("$projectPath/.nais/naiserator.yaml")

    @Test
    fun `liveness probe configured in nais manifest is available`() {
        GithubMock().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, nais.spec.liveness.path)) {
                    response shouldHaveStatus 200
                }
            }
        }
    }

    @Test
    fun `readiness probe configured in nais manifest is available`() {
        GithubMock().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, nais.spec.readiness.path)) {
                    response shouldHaveStatus 200
                }
            }
        }
    }

    @Test
    fun `metrics endpoint configured in nais manifest is available`() {
        GithubMock().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, nais.spec.prometheus.path)) {
                    response shouldHaveStatus 200
                }
            }
        }
    }
}
