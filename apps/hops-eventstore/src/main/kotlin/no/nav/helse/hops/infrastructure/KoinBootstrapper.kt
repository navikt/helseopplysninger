package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.EventStoreReadOnlyRepository
import no.nav.helse.hops.domain.EventStoreRepository
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val datasource: EventStoreRepositoryExposedORM.Config)
        single { loadConfigsOrThrow<ConfigRoot>("/application.conf", "/application.properties") }
        single { get<ConfigRoot>().datasource }
        single<EventStoreRepository> { EventStoreRepositoryExposedORM(get()) }
        single<EventStoreReadOnlyRepository> { get<EventStoreRepository>() }
        single { FhirMessageProcessService(get()) }
        single { FhirMessageSearchService(get()) }
    }
}
