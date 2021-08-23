package test.external.infrastructure

import java.net.URL

data class TestExternalConfig(val externalApi: ExternalApi) {
    data class ExternalApi(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientJwk: String,
        val scope: String,
        val audience: String,
    )
}
