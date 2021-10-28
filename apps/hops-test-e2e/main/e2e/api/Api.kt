package e2e.api

import e2e._common.Liveness
import e2e._common.Test
import e2e.api.tests.ApiPublish
import e2e.api.tests.ApiSubscribe
import e2e.http.HttpClientFactory
import e2e.http.HttpFeature.MASKINPORTEN
import e2e.kafka.KafkaConfig
import e2e.kafka.KafkaFactory
import e2e.kafka.KafkaFhirFlow
import e2e.replay.ReplayConfig
import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import kotlinx.coroutines.Dispatchers
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.apiTests(): List<Test> {
    val config = loadConfigsOrThrow<ApiConfig>()

    val api = ApiExternalClient(
        config = config,
        httpClient = HttpClientFactory.create(config.api.maskinporten, MASKINPORTEN),
    )

    val context = Dispatchers.Default
    val kafka = KafkaFhirFlow(
        consumer = KafkaFactory.createConsumer(config.kafka),
        topic = config.kafka.topic.published,
        context = context,
    )

    environment.monitor.subscribe(ApplicationStopping) {
        environment.log.info("closing kafka consuming flow...")
        kafka.close()
    }

    return listOf(
        Liveness("api liveness", config.api.host),
        ApiPublish("publish external", api, kafka, context),
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
