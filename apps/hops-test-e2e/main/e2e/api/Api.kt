package e2e.api

import e2e._common.E2eTest
import e2e._common.LivenessTest
import e2e.api.tests.ApiPublishTest
import e2e.api.tests.ApiSubscribeTest
import io.ktor.application.Application
import no.nav.helse.hops.hoplite.loadConfigsOrThrow

internal fun Application.apiTests(): List<E2eTest> {
    val config = loadConfigsOrThrow<ApiConfig>("/application.yaml")
    val externalClient = ApiExternalClient(HttpClientFactory.create(config.api.maskinporten), config)

    return listOf(
        E2eTest { LivenessTest("api liveness", config.api.host) },
        E2eTest { ApiPublishTest(externalClient) },
        E2eTest { ApiSubscribeTest(externalClient) }
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
