package no.nav.helse.hops

import io.ktor.config.*
import no.nav.helse.hops.common.Oauth2ProviderConfig
import no.nav.helse.hops.fkr.*
import org.koin.dsl.module

val CONFIG_NAMESPACE = "no.nav.helse.hops.fkr"

val koinModule = module {
    single<FkrFacade> {
        val fkrAppConfig = get<ApplicationConfig>().config(CONFIG_NAMESPACE)
        fun getString(path: String): String = fkrAppConfig.property(path).getString()

        val hostUrl = fkrAppConfig.property("hostUrl").getString()

        val oauth2Config = Oauth2ProviderConfig(
            getString("tokenUrl"),
            getString("clientId"),
            getString("clientSecret"),
            getString("scope"))

        return@single FkrFacadeImpl(hostUrl, oauth2Config)
    }
}