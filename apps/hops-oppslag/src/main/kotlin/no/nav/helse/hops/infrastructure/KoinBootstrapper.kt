package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import no.nav.helse.hops.domain.FkrFacade
import no.nav.helse.hops.domain.FkrFacadeImpl
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import no.nav.helse.hops.security.fhir.OauthRequestInterceptor
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val kontaktregister: Configuration.Kontaktregister)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kontaktregister }
        single { FhirContext.forR4() }
        single { createHapiFhirClient(get(), get()) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }

    private fun createHapiFhirClient(config: Configuration.Kontaktregister, ctx: FhirContext): IGenericClient {
        val oauthClient = OAuth2ClientFactory.create(
            config.discoveryUrl, config.clientId, config.clientSecret
        )

        val interceptor = OauthRequestInterceptor(oauthClient, config.scope)

        // So that we dont start by requesting /metadata.
        val factory = ctx.restfulClientFactory.apply { serverValidationMode = ServerValidationModeEnum.NEVER }
        return factory.newGenericClient(config.baseUrl).apply { registerInterceptor(interceptor) }
    }
}