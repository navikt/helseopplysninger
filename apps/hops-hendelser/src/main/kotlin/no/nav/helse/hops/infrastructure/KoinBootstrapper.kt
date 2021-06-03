package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.MessageBusProducer
import no.nav.helse.hops.domain.TaskChangeFeed
import no.nav.helse.hops.domain.TaskChangeToMessageResponseMapper
import no.nav.helse.hops.domain.TaskStateChangeSubscriberJob
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.FhirClientHapi
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

        single { FhirClientFactory.createWithAuth(get()) }
        single<FhirClient> { FhirClientHapi(get()) }
        single<FhirClientReadOnly> { get<FhirClient>() }
        single<TaskChangeFeed> { FhirHistoryFeedHapi(get()) }
        single { TaskChangeToMessageResponseMapper(get()) }
        single<MessageBusProducer> { MessageBusProducerKafka(get(), get()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            TaskStateChangeSubscriberJob(get(), get(), get(), get(), get(), getLogger<TaskStateChangeSubscriberJob>())
        }
    }
}

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
