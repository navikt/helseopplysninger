package no.nav.helse.hops.fkr

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import io.ktor.config.*
import no.nav.helse.hops.security.Oauth2ClientProviderConfig
import no.nav.helse.hops.security.OauthRequestInterceptor
import org.koin.dsl.module

object FkrKoinModule {

    val instance = module {
        single { FhirContext.forR4() }
        single { createHapiFhirClient(get<ApplicationConfig>().config("no.nav.helse.hops.fkr"), get()) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }

    private fun createHapiFhirClient(appConfig: ApplicationConfig, ctx: FhirContext): IGenericClient {
        fun getString(path: String): String = appConfig.property(path).getString()

        val oauth2Config = Oauth2ClientProviderConfig(
            getString("tokenUrl"),
            getString("clientId"),
            getString("clientSecret"),
            getString("scope"))

        val interceptor = OauthRequestInterceptor(oauth2Config)

        // So that we dont start by requesting /metadata.
        val factory = ctx.restfulClientFactory.apply { serverValidationMode = ServerValidationModeEnum.NEVER }
        return factory.newGenericClient(getString("baseUrl")).apply { registerInterceptor(interceptor) }
    }
}

