package no.nav.helse.hops

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import no.nav.helse.hops.security.fhir.OauthRequestInterceptor
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import java.net.URL

object FhirClientFactory {
    data class Config(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )

    /** Creates a FHIR http-client configured with an oauth2 interceptor. **/
    fun create(config: Config): IGenericClient {
        val oauthClient = OAuth2ClientFactory.create(
            config.discoveryUrl.toString(), config.clientId, config.clientSecret
        )

        val interceptor = OauthRequestInterceptor(oauthClient, config.scope)
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        val factory = ctx.restfulClientFactory.apply {
            // So that we dont start by requesting '/metadata'.
            serverValidationMode = ServerValidationModeEnum.NEVER
        }

        return factory.newGenericClient(config.baseUrl.toString()).apply {
            registerInterceptor(interceptor)
        }
    }
}
