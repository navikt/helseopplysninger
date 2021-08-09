package infrastructure

import domain.FkrFacade
import domain.FkrFacadeImpl
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.client.FhirClientHapi
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import org.koin.dsl.module

object KoinBootstrapper {

    val module = module {
        data class ConfigRoot(val kontaktregister: FhirClientFactory.Config)
        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kontaktregister }
        single<FhirClientReadOnly> { FhirClientHapi(FhirClientFactory.createWithAuth(get())) }
        single<FkrFacade> { FkrFacadeImpl(get()) }
    }
}
