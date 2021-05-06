package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.HapiFacade
import no.nav.helse.hops.domain.HapiFacadeImpl
import no.nav.helse.hops.fhir.OauthRequestInterceptor
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val hapiserver: Configuration.Hapiserver)
        single { ConfigLoader().loadConfigOrThrow<ConfigRoot>("/application.conf") }
        single { get<ConfigRoot>().hapiserver }
        single { FhirContext.forR4() }
        single { createHapiFhirClient(get(), get()) }
        single<HapiFacade> { HapiFacadeImpl(get()) }
    }

    private fun createHapiFhirClient(config: Configuration.Hapiserver, ctx: FhirContext): IGenericClient {
        val oauthClient = OAuth2ClientFactory.create(
            config.discoveryUrl, config.clientId, config.clientSecret
        )

        val interceptor = OauthRequestInterceptor(oauthClient, config.scope)

        // So that we dont start by requesting /metadata.
        val factory = ctx.restfulClientFactory.apply { serverValidationMode = ServerValidationModeEnum.NEVER }
        return factory.newGenericClient(config.baseUrl).apply { registerInterceptor(interceptor) }
    }
}
