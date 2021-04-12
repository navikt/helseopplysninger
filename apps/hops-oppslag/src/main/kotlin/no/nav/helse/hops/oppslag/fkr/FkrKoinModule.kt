package no.nav.helse.hops.oppslag.fkr

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import io.ktor.config.ApplicationConfig
import no.nav.helse.hops.oppslag.security.OauthRequestInterceptor
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import org.koin.dsl.module

object FkrKoinModule {

    val instance = module {
        single { FhirContext.forCached(FhirVersionEnum.R4) }
        single { createHapiFhirClient(get<ApplicationConfig>().config("no.nav.helse.hops.fkr")) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }

    private fun createHapiFhirClient(appConfig: ApplicationConfig): IGenericClient {
        fun getString(path: String): String = appConfig.property(path).getString()

        val oauth2Client = OAuth2ClientFactory.create(
            getString("wellKnownUrl"),
            getString("clientId"),
            getString("clientSecret")
        )

        val interceptor = OauthRequestInterceptor(oauth2Client, getString("scope"))

        val ctx = FhirContext.forCached(FhirVersionEnum.R4)!!

        // So that we dont start by requesting /metadata.
        val factory = ctx.restfulClientFactory.apply { serverValidationMode = ServerValidationModeEnum.NEVER }
        return factory.newGenericClient(getString("baseUrl")).apply { registerInterceptor(interceptor) }
    }
}
