package no.nav.helse.hops.infrastructure

/** Container for typesafe configuration classes. **/
object Configuration {
    data class Kafka(
        val brokers: String,
        val groupId: String,
        val topic: String,
        val clientId: String,
        val truststorePath: String,
        val keystorePath: String,
        val credstorePsw: String
    )
}
