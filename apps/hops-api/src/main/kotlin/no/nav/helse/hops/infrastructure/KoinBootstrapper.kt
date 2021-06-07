package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.FhirClientHapi
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val hapiserver: FhirClientFactory.Config)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().hapiserver }
        single<FhirClient> { FhirClientHapi(FhirClientFactory.createWithAuth(get())) }
        single<FhirClientReadOnly> { get<FhirClient>() }
        single { FhirMessageProcessService(get()) }
    }
}
