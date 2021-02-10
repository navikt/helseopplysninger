package no.nav.helse.hops.fkr

import ca.uhn.fhir.context.FhirContext
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import io.ktor.config.*
import io.ktor.http.*
import no.nav.helse.hops.security.Oauth2ClientProviderConfig
import no.nav.helse.hops.security.oauth2
import org.koin.core.qualifier.named
import org.koin.dsl.module

object FkrKoinModule {
    val CLIENT = named("kontaktregister")

    val instance = module {
        single(CLIENT) { createFkrHttpClient(get<ApplicationConfig>().config("no.nav.helse.hops.fkr")) }
        single { FhirContext.forR4() }
        single<FkrFacade> { FkrFacadeImpl(get(CLIENT), get()) }
    }

    private fun createFkrHttpClient(appConfig: ApplicationConfig): HttpClient {
        fun getString(path: String): String = appConfig.property(path).getString()
        val oauth2Config = Oauth2ClientProviderConfig(
            getString("tokenUrl"),
            getString("clientId"),
            getString("clientSecret"),
            getString("scope"),
            true)

        return HttpClient {
            install(Auth)
            {
                oauth2(oauth2Config)
            }
            defaultRequest {
                val baseUrl = Url(getString("baseUrl"))
                host = baseUrl.host
                port = baseUrl.port
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
    }
}

