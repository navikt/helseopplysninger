package no.nav.helse.hops.fkr

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.config.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.security.token.support.client.core.context.JwtBearerTokenResolver
import no.nav.security.token.support.client.core.oauth2.*
import java.util.*

fun Route.getPractitioner() {
    get("/Practitioner/") {
        val token = GetAccessToken(application.environment.config)
        call.respondText(token ?: "not found")
    }
}

private fun GetAccessToken(applicationConfig: ApplicationConfig): String? {
    val accessTokenService = OAuth2AccessTokenService(
        object : JwtBearerTokenResolver {
            override fun token(): Optional<String> = Optional.empty()
        },
        null, // N/A
        ClientCredentialsTokenClient(DefaultOAuth2HttpClient()),
        null // N/A
    )

    val clientPropertiesConfig = ClientPropertiesConfig(applicationConfig)
    val clientProperties = clientPropertiesConfig.configFor("helse_id")
    val token = accessTokenService.getAccessToken(clientProperties)

    return token?.accessToken
}

private fun ClientPropertiesConfig.configFor(client: String) =
    this.clientConfig[client] ?: throw RuntimeException("$client do not exist in configuration.")