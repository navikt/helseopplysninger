package e2e.sink

import e2e._common.E2eTest
import e2e._common.LivenessTest
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.sinkTests(): List<E2eTest> {
    val config = loadConfigsOrThrow<SinkConfig>("/application.yaml")

    return listOf(
        E2eTest { LivenessTest("event-sink liveness", config.sink.host) }
    )
}

internal data class SinkConfig(val sink: Sink) {
    data class Sink(
        val host: String,
    )
}
