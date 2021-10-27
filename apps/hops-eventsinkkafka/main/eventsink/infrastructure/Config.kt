package eventsink.infrastructure

import no.nav.helse.hops.plugin.KafkaConfig
import java.net.URL

data class Config(val kafka: KafkaConfig, val eventStore: EventStore) {
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
