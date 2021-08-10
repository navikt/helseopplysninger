package infrastructure

import domain.ExternalApiFacade
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import no.nav.helse.hops.koin.singleClosable
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinBootstrapper {
    val module = module {
        data class ConfigRoot(
            val externalApi: Configuration.ExternalApi
        )

        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().externalApi }

        singleClosable(named(EXTERNAL_API_CLIENT_NAME)) { HttpClientFactory.create(get()) }
        single<ExternalApiFacade> { ExternalApiHttp(get(named(EXTERNAL_API_CLIENT_NAME)), get()) }
    }
}

const val EXTERNAL_API_CLIENT_NAME = "external-api"
