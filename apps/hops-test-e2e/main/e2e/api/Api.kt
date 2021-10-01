package e2e.api

import e2e._common.Liveness
import e2e._common.Test
import e2e.api.tests.ApiPublish
import e2e.api.tests.ApiSubscribe
import e2e.kafka.KafkaConfig
import e2e.kafka.KafkaFactory
import e2e.kafka.FhirKafkaListener
import e2e.replay.ReplayConfig
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.apiTests(): List<Test> {
    val config = loadConfigsOrThrow<ApiConfig>("/application.yaml")
    val api = ApiExternalClient(HttpClientFactory.create(config.api.maskinporten), config)
    val flow = FhirKafkaListener(KafkaFactory.createConsumer(config.kafka), config.kafka.topic.published)

    return listOf(
        Liveness("api liveness", config.api.host),
        ApiPublish("publish external", api, flow),
        ApiSubscribe("subscribe external", api)
    )
}

internal data class ApiConfig(
    val api: Api,
    val replay: ReplayConfig.Replay,
    val kafka: KafkaConfig.Kafka,
) {
    data class Api(
        val host: String,
        val hostExternal: String,
        val maskinporten: Maskinporten
    )

    data class Maskinporten(
        val discoveryUrl: String,
        val clientId: String,
        val clientJwk: String,
        val scope: String,
        val audience: String,
        val issuer: String,
    )
}
