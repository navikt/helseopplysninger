package e2e.store

import e2e._common.Liveness
import e2e._common.Test
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.storeTests(): List<Test> {
    val config = loadConfigsOrThrow<StoreConfig>()

    return listOf(
        Liveness("event-store liveness", config.store.host)
    )
}

internal data class StoreConfig(val store: Store) {
    data class Store(
        val host: String,
    )
}
