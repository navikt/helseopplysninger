package no.nav.helse.hops.infrastructure

/** Container for typesafe configuration classes. **/
object Configuration {
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

    data class FhirMessaging(
        val endpoint: String,
    )

    data class FhirServer(
        val baseUrl: String,
        val discoveryUrl: String,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
