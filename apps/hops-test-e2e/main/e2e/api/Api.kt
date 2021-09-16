package e2e.api

import e2e._common.Liveness
import e2e._common.Test
import e2e.api.tests.ApiPublish
import e2e.api.tests.ApiSubscribe
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.apiTests(): List<Test> {
    val config = loadConfigsOrThrow<ApiConfig>("/application.yaml")
    val externalClient = ApiExternalClient(HttpClientFactory.create(config.api.maskinporten), config)

    return listOf(
        Liveness("api liveness", config.api.host),
        ApiPublish("publish external", externalClient),
        ApiSubscribe("subscribe external", externalClient)
    )
}

internal data class ApiConfig(val api: Api) {
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
    )
}
