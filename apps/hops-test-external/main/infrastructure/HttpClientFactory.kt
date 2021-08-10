package infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import no.nav.helse.hops.security.OAuth2Provider
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory

object HttpClientFactory {
    fun create(config: Configuration.ExternalApi) =
        HttpClient {
            install(Auth) {
                val oauth2Client = OAuth2ClientFactory.createJwk(
                    config.discoveryUrl.toString(), config.clientId, config.clientJwk
                )
                providers.add(OAuth2Provider(oauth2Client, config.scope))
            }
        }
}
