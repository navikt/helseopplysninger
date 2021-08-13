package infrastructure

import java.net.URL

/** Container for typesafe configuration classes. **/
object Configuration {
    data class ExternalApi(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientJwk: String,
        val scope: String,
        val audience: String,
    )
}
