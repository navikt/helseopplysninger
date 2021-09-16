package e2e.store

import e2e._common.E2eTest
import e2e._common.LivenessTest
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.storeTests(): List<E2eTest> {
    val config = loadConfigsOrThrow<StoreConfig>("/application.yaml")

    return listOf(
        E2eTest { LivenessTest("event-store liveness", config.store.host) }
    )
}

internal data class StoreConfig(val store: Store) {
    data class Store(
        val host: String,
    )
}
