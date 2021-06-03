package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.FkrFacade
import no.nav.helse.hops.domain.FkrFacadeImpl
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.client.FhirClientHapiGenericClient
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val kontaktregister: FhirClientFactory.Config)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kontaktregister }
        single<FhirClientReadOnly> { FhirClientHapiGenericClient(FhirClientFactory.createWithAuth(get())) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }
}
