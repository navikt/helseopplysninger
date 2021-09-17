package no.nav.helse.hops

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener

class KotestSetup : ProjectListener, AbstractProjectConfig() {
    override fun listeners() = listOf(KotestSetup())
    override suspend fun beforeProject() {
        MockServers.oAuth.start()
    }

    override suspend fun afterProject() {
        MockServers.oAuth.shutdown()
    }
}
