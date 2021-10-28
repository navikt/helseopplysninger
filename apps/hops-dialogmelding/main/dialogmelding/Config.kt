package dialogmelding

import no.nav.helse.hops.plugin.KafkaConfig
import java.net.URL

data class Config(
    val kafka: KafkaConfig,
    val fhirJsonToPdfConverter: Endpoint
) {
    data class Endpoint(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
