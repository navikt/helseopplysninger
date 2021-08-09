package no.nav.helse.hops.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import no.nav.helse.hops.hops.security.oauth.OAuth2ClientFactory
import java.net.URL

object FhirClientFactory {
    data class Config(
        val baseUrl: URL,
        val discoveryUrl: URL,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )

    /** Creates a FHIR http-client. **/
    fun create(baseUrl: URL): IGenericClient {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        val factory = ctx.restfulClientFactory.apply {
            // So that we dont start by requesting '/metadata'.
            serverValidationMode = ServerValidationModeEnum.NEVER
        }

        return factory.newGenericClient(baseUrl.toString())
    }

    /** Creates a FHIR http-client configured with an OAuth2 interceptor. **/
    fun createWithAuth(config: Config): IGenericClient {
        val oauthClient = OAuth2ClientFactory.create(
            config.discoveryUrl.toString(), config.clientId, config.clientSecret
        )

        val interceptor = OauthRequestInterceptor(oauthClient, config.scope)
        return create(config.baseUrl).apply { registerInterceptor(interceptor) }
    }
}
