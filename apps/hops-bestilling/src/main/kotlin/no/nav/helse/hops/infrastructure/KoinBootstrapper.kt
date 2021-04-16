package no.nav.helse.hops.infrastructure

import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.FhirMessageProcessor
import no.nav.helse.hops.domain.FhirMessageProcessorImpl
import no.nav.helse.hops.koin.HttpRequestKoinScope
import no.nav.helse.hops.koin.scopedClosable
import no.nav.helse.hops.koin.singleClosable
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KoinBootstrapper {
    val module = module {
        data class ConfigRoot(val kafka: Configuration.Kafka)
        single { ConfigLoader().loadConfigOrThrow<ConfigRoot>("/application.conf") }
        single { get<ConfigRoot>().kafka }
        single<FhirMessageProcessor> { FhirMessageProcessorImpl(getLogger<FhirMessageProcessorImpl>()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) { BestillingConsumerJob(get(), get(), getLogger<BestillingConsumerJob>(), get()) }

        scope<HttpRequestKoinScope> {
            scopedClosable { BestillingProducerJob(get(), get()) }
        }
    }

    private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
}