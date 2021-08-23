package infrastructure

import java.net.URL

data class ApiConfig(val eventStore: EventStore) {
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
