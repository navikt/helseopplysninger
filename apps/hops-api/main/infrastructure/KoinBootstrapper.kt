package infrastructure

import domain.EventStore
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import no.nav.helse.hops.koin.singleClosable
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinBootstrapper {
    val module = module {
        data class ConfigRoot(
            val eventStore: Configuration.EventStore
        )

        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().eventStore }

        singleClosable(named(EVENT_STORE_CLIENT_NAME)) { HttpClientFactory.create(get()) }
        single<EventStore> { EventStoreHttp(get(named(EVENT_STORE_CLIENT_NAME)), get()) }
    }
}

const val EVENT_STORE_CLIENT_NAME = "eventStore"
