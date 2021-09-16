package e2e.replay

import e2e._common.E2eTest
import e2e._common.LivenessTest
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.replayTests(): List<E2eTest> {
    val config = loadConfigsOrThrow<ReplayConfig>("/application.yaml")

    return listOf(
        E2eTest { LivenessTest("event-replay liveness", config.replay.host) }
    )
}

internal data class ReplayConfig(val replay: Replay) {
    data class Replay(
        val host: String,
    )
}
