package no.nav.helse.hops.infrastructure

import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.Service
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

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        single { Service(get(), get(), getLogger<Service>()) }
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
