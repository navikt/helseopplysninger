package e2e.replay

import e2e._common.Liveness
import e2e._common.Test
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.replayTests(): List<Test> {
    val config = loadConfigsOrThrow<ReplayConfig>()

    return listOf(
        Liveness("event-replay liveness", config.replay.host)
    )
}

internal data class ReplayConfig(val replay: Replay) {
    data class Replay(
        val host: String,
    )
}
