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
    "API_HOST" to HopsMocks.api.getBaseUrl(),
    "EVENTREPLAYKAFKA_HOST" to HopsMocks.eventreplay.getBaseUrl(),
    "EVENTSINKKAFKA_HOST" to HopsMocks.eventsink.getBaseUrl(),
    "EVENTSTORE_HOST" to HopsMocks.eventstore.getBaseUrl(),
    "FILESHARE_HOST" to HopsMocks.fileshare.getBaseUrl(),
)

class KotestSetup : ProjectListener, AbstractProjectConfig() {
    override fun listeners() = listOf(KotestSetup())
    override suspend fun beforeProject() {
        HopsMocks.api.start()
        HopsMocks.eventreplay.start()
        HopsMocks.eventsink.start()
        HopsMocks.eventstore.start()
        HopsMocks.fileshare.start()
    }

    override suspend fun afterProject() {
        HopsMocks.api.shutdown()
        HopsMocks.eventreplay.shutdown()
        HopsMocks.eventsink.shutdown()
        HopsMocks.eventstore.shutdown()
        HopsMocks.fileshare.shutdown()
    }
}
