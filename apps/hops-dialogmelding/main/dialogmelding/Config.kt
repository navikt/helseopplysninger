package dialogmelding

import no.nav.helse.hops.plugin.KafkaConfig
import java.net.URL

data class Config(
    val kafka: KafkaConfig,
    val fhirJsonToPdfConverter: Endpoint,
    val messageQueue: MQ
) {
    data class Endpoint(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )

    data class MQ(
        val applicationName: String,
        val queueManager: String,
        val host: String,
        val port: Int,
        val channel: String,
        val queue: String,
        val user: String,
        val password: String
    )
}
