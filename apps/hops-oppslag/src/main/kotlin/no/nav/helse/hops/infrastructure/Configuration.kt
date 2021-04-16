package no.nav.helse.hops.infrastructure

/** Container for typesafe configuration classes. **/
object Configuration {
    data class Kontaktregister(
        val baseUrl: String,
        val discoveryUrl: String,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
