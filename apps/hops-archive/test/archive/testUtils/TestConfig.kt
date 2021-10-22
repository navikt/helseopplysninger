package archive.testUtils

import archive.module
import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

fun <R> withTestApp(mocks: Mocks, test: TestApplicationEngine.() -> R): R {
    val config = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to mocks.oauth.azureWellKnownUrl().toString(),
        "DOKARKIV_BASE_URL" to mocks.dokarkiv.url,
        "CONVERT_BASE_URL" to mocks.converter.url,
        "KAFKA_BROKERS" to mocks.kafka.getHost(),
    )

    return EnvironmentVariables(config).execute<R> {
        withTestApplication(Application::module, test)
    }
}
