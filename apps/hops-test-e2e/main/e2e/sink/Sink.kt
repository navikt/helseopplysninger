package e2e.sink

import e2e._common.Liveness
import e2e._common.Test
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.sinkTests(): List<Test> {
    val config = loadConfigsOrThrow<SinkConfig>()

    return listOf(
        Liveness("event-sink liveness", config.sink.host)
    )
}

internal data class SinkConfig(val sink: Sink) {
    data class Sink(
        val host: String,
    )
}
