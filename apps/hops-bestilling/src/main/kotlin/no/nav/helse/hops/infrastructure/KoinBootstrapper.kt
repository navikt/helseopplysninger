package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.BestillingConsumerJob
import no.nav.helse.hops.domain.FhirMessageBus
import no.nav.helse.hops.domain.FhirRepository
import no.nav.helse.hops.domain.FhirRepositoryImpl
import no.nav.helse.hops.domain.FhirResourceValidator
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.FhirClientHapiGenericClient
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import no.nav.helse.hops.koin.singleClosable
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KoinBootstrapper {
    val singleModule = module {
        data class ConfigRoot(
            val kafka: Configuration.Kafka,
            val fhirMessaging: Configuration.FhirMessaging,
            val fhirServer: FhirClientFactory.Config
        )

        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kafka }
        single { get<ConfigRoot>().fhirMessaging }
        single { get<ConfigRoot>().fhirServer }

        single<FhirResourceValidator> { FhirResourceValidatorHapi }
        single<FhirMessageBus> { FhirMessageBusKafka(get(), get(), get()) }
        single<FhirClient> { FhirClientHapiGenericClient(FhirClientFactory.createWithAuth(get())) }
        single<FhirClientReadOnly> { get<FhirClient>() }
        single<FhirRepository> { FhirRepositoryImpl(get(), getLogger<FhirRepositoryImpl>()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            BestillingConsumerJob(get(), getLogger<BestillingConsumerJob>(), get(), get(), get())
        }
    }
}

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
