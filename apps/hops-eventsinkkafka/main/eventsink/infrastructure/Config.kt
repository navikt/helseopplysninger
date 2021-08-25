package eventsink.infrastructure

import java.net.URL

data class Config(val kafka: Kafka, val eventStore: EventStore) {
    data class Kafka(
        val brokers: String,
        val groupId: String,
        val topic: String,
        val clientId: String,
        val security: Boolean,
        val truststorePath: String,
        val keystorePath: String,
        val credstorePsw: String
    )

    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
