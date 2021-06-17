package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.FhirMessageBus
import no.nav.helse.hops.domain.EventSinkJob
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

        singleClosable(named("eventStore")) { HttpClientFactory.create(get()) }
        single<EventStore> { EventStoreHttp(get(), get(named("eventStore"))) }
        single<FhirMessageBus> { FhirMessageBusKafka(get(), get()) }

        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            EventSinkJob(get(), getLogger<EventSinkJob>(), get())
        }
    }
}

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
