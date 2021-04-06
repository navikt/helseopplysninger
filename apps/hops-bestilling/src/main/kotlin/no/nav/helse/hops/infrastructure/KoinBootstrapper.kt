package no.nav.helse.hops.infrastructure

import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.FhirMessageProcessor
import no.nav.helse.hops.domain.FhirMessageProcessorImpl
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable

object KoinBootstrapper {
    val module = module {
        data class ConfigRoot(val kafka: Configuration.Kafka)
        single { ConfigLoader().loadConfigOrThrow<ConfigRoot>("/application.properties") }
        single { get<ConfigRoot>().kafka }
        single<FhirMessageProcessor> { FhirMessageProcessorImpl(getLogger<FhirMessageProcessorImpl>()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
//        singleClosable(createdAtStart = true) { BestillingProducerJob(get(), get()) }
        singleClosable(createdAtStart = true) { BestillingConsumerJob(get(), get(), get()) }
    }

    /** Helper function to register Closeable as singleton and tie its lifetime to the Module. **/
    private inline fun <reified T : Closeable> org.koin.core.module.Module.singleClosable(
        qualifier: Qualifier? = null,
        createdAtStart: Boolean = false,
        override: Boolean = false,
        noinline definition: Definition<T>
    ): BeanDefinition<T> = single(qualifier, createdAtStart, override, definition).onClose { it?.close() }

    private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
}
