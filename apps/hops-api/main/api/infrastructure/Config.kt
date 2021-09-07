package api.infrastructure

import java.net.URL
import no.nav.helse.hops.hoplite.OauthIssuerConfig

data class Config(
    val oauth: ModuleOAuth,
    val eventStore: EventStore
) {
    data class ModuleOAuth(
        val issuers: List<OauthIssuerConfig>,
        val publishScope: String,
        val subscribeScope: String
    )
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
