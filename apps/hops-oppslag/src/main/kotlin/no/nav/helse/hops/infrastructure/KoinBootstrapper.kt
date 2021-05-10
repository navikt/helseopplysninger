package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import no.nav.helse.hops.domain.FkrFacade
import no.nav.helse.hops.domain.FkrFacadeImpl
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val kontaktregister: FhirClientFactory.Config)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kontaktregister }
        single { FhirContext.forR4() }
        single { FhirClientFactory.createWithAuth(get()) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }
}
