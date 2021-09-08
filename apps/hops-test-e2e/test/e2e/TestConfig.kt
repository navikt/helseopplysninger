package e2e

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener
import io.kotest.extensions.system.withEnvironment
import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication

fun <R> withTestApp(test: TestApplicationEngine.() -> R): R =
    withEnvironment(config) {
        withTestApplication(Application::main, test)
    }

private val config = mapOf(
    "GITHUB_URL" to Mocks.github.getBaseUrl(),
    "API_HOST" to Mocks.hops.getBaseUrl(),
    "EVENTREPLAYKAFKA_HOST" to Mocks.hops.getBaseUrl(),
    "EVENTSINKKAFKA_HOST" to Mocks.hops.getBaseUrl(),
    "EVENTSTORE_HOST" to Mocks.hops.getBaseUrl(),
    "FILESHARE_HOST" to Mocks.hops.getBaseUrl(),
    "TEST_EXTERNAL_HOST" to Mocks.hops.getBaseUrl(),
)

class KotestSetup : ProjectListener, AbstractProjectConfig() {
    override fun listeners() = listOf(KotestSetup())
    override suspend fun beforeProject() {
        Mocks.github.start()
        Mocks.hops.start()
    }

    override suspend fun afterProject() {
        Mocks.github.shutdown()
        Mocks.hops.shutdown()
    }
}
