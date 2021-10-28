package questionnaire.infrastructure

import java.net.URL
import no.nav.helse.hops.security.MaskinportenProvider

data class Config(
    val oauth: ModuleOAuth,
    val eventStore: EventStore
) {
    data class ModuleOAuth(
        val maskinporten: MaskinportenProvider.Configuration,
    )
    data class EventStore(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )
}
