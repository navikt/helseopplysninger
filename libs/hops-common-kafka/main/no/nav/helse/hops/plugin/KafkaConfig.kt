package no.nav.helse.hops.plugin

data class KafkaConfig(
    val brokers: String,
    val groupId: String,
    val topic: String,
    val clientId: String,
    val security: Boolean,
    val truststorePath: String,
    val keystorePath: String,
    val credstorePsw: String
)
