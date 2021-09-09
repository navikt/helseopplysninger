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
    "HOPS_DOMAIN" to "",
    "API_HOST" to Mocks.api.getBaseUrl(),
    "EVENTREPLAYKAFKA_HOST" to Mocks.eventreplay.getBaseUrl(),
    "EVENTSINKKAFKA_HOST" to Mocks.eventsink.getBaseUrl(),
    "EVENTSTORE_HOST" to Mocks.eventstore.getBaseUrl(),
    "FILESHARE_HOST" to Mocks.fileshare.getBaseUrl(),
)

class KotestSetup : ProjectListener, AbstractProjectConfig() {
    override fun listeners() = listOf(KotestSetup())
    override suspend fun beforeProject() {
        Mocks.api.start()
        Mocks.eventreplay.start()
        Mocks.eventsink.start()
        Mocks.eventstore.start()
        Mocks.fileshare.start()
    }

    override suspend fun afterProject() {
        Mocks.api.shutdown()
        Mocks.eventreplay.shutdown()
        Mocks.eventsink.shutdown()
        Mocks.eventstore.shutdown()
        Mocks.fileshare.shutdown()
    }
}
