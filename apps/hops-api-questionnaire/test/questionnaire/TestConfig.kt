package questionnaire

import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

fun <R> withTestApp(github: GithubMock, test: TestApplicationEngine.() -> R): R {
    val config = mapOf(
        "GITHUB_API_URL" to github.url,
    )

    return EnvironmentVariables(config).execute<R> {
        withTestApplication(Application::module, test)
    }
}
