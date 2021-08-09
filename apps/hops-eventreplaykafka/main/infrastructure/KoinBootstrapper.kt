package infrastructure

import domain.EventReplayJob
import domain.EventStore
import domain.FhirMessageBus
import no.nav.helse.hops.hoplite.loadConfigOrThrow
import no.nav.helse.hops.koin.singleClosable
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KoinBootstrapper {
    val module = module {
        data class ConfigRoot(
            val kafka: Configuration.Kafka,
            val eventStore: Configuration.EventStore
        )

        single { loadConfigOrThrow<ConfigRoot>() }
        single { get<ConfigRoot>().kafka }
        single { get<ConfigRoot>().eventStore }

        singleClosable(named(EVENT_STORE_CLIENT_NAME)) { HttpClientFactory.create(get()) }
        single<EventStore> { EventStoreHttp(get(), get(named(EVENT_STORE_CLIENT_NAME))) }
        single<FhirMessageBus> { FhirMessageBusKafka(get(), get(), get()) }

        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable(createdAtStart = true) {
            EventReplayJob(get(), getLogger<EventReplayJob>(), get())
        }
    }
}

private const val EVENT_STORE_CLIENT_NAME = "eventStore"
private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
