package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.HapiFacade
import no.nav.helse.hops.domain.HapiFacadeImpl
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val hapiserver: FhirClientFactory.Config)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().hapiserver }
        single { FhirClientFactory.createWithAuth(get()) }
        single<HapiFacade> { HapiFacadeImpl(get()) }
    }
}
