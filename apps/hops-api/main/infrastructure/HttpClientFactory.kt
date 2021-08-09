package infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import no.nav.helse.hops.hops.security.oauth.OAuth2ClientFactory
import no.nav.helse.hops.security.OAuth2Provider

object HttpClientFactory {
    fun create(config: Configuration.EventStore) =
        HttpClient {
            install(Auth) {
                val oauth2Client = OAuth2ClientFactory.create(
                    config.discoveryUrl.toString(), config.clientId, config.clientSecret
                )
                providers.add(OAuth2Provider(oauth2Client, config.scope))
            }
        }
}
