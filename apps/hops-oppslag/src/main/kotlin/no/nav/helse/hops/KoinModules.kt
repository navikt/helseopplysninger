package no.nav.helse.hops

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.config.*
import no.nav.helse.hops.common.Oauth2ProviderConfig
import no.nav.helse.hops.common.oauth2
import no.nav.helse.hops.fkr.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

val CONFIG_NAMESPACE = "no.nav.helse.hops.fkr"
val FKR_CLIENT = named("kontaktregister")

val koinModule = module {
    single<FkrFacade> {
        val hostUrl = get<ApplicationConfig>().property("${CONFIG_NAMESPACE}.hostUrl").getString()
        return@single FkrFacadeImpl(hostUrl, get(FKR_CLIENT))
    }
    single(FKR_CLIENT) { createFkrHttpClient(get<ApplicationConfig>().config(CONFIG_NAMESPACE)) }
}

private fun createFkrHttpClient(appConfig: ApplicationConfig): HttpClient {
    fun getString(path: String): String = appConfig.property(path).getString()

    val oauth2Config = Oauth2ProviderConfig(
        getString("tokenUrl"),
        getString("clientId"),
        getString("clientSecret"),
        getString("scope"))

    return HttpClient {
        install(Auth)
        {
            oauth2(oauth2Config)
        }
    }
}