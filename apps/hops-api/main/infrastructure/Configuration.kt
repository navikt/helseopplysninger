package infrastructure

import java.net.URL

/** Container for typesafe configuration classes. **/
object Configuration {
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
