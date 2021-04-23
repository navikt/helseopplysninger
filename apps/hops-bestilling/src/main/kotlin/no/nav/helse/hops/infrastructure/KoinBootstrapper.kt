package no.nav.helse.hops.infrastructure

import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.BestillingConsumerJob
import no.nav.helse.hops.domain.BestillingProducerJob
import no.nav.helse.hops.domain.FhirResourceValidator
import no.nav.helse.hops.domain.MessageBus
import no.nav.helse.hops.koin.HttpRequestKoinScope
import no.nav.helse.hops.koin.scopedClosable
import no.nav.helse.hops.koin.singleClosable
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KoinBootstrapper {
    val singleModule = module {
        data class ConfigRoot(val kafka: Configuration.Kafka, val fhirMessaging: Configuration.FhirMessaging)
        single { ConfigLoader().loadConfigOrThrow<ConfigRoot>("/application.conf") }
        single { get<ConfigRoot>().kafka }
        single { get<ConfigRoot>().fhirMessaging }

        single<FhirResourceValidator> { FhirResourceValidatorHapi }
        single<MessageBus> { MessageBusKafka(get(), get(), get()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            BestillingConsumerJob(get(), getLogger<BestillingConsumerJob>(), get(), get())
        }
    }

    val scopeModule = module {
        scope<HttpRequestKoinScope> {
            scopedClosable { BestillingProducerJob(get(), get(), get()) }
        }
    }

    private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
}
