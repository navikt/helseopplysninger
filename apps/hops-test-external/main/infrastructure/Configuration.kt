package infrastructure

import java.net.URL

/** Container for typesafe configuration classes. **/
object Configuration {
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val scopes: String,
        val clientJwk: String,
    )
}
