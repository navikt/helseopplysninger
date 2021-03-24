package no.nav.helse.hops.koinBootstrapping

import no.nav.helse.hops.Service
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.Properties

object Bootstrapper {
    val koinModule = module {
        singleClosable { createProducer(get()) }
        singleClosable { createConsumer(get()) }
        single { Service(get(), get(), getLogger<Service>()) }
    }
}

/** Register Closeable as singleton and tie its lifetime to the Module. **/
private inline fun <reified T : Closeable> org.koin.core.module.Module.singleClosable(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = single(qualifier, createdAtStart, override, definition).onClose { it?.close() }

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

private fun createProducer(config: Configuration.Kafka): Producer<Unit, IBaseResource> {
    val props = Properties().apply {
        put("bootstrap.servers", config.host)
        put("key.serializer", VoidSerializer::class.java)
        put("value.serializer", FhirResourceSerializer::class.java)
    }
    return KafkaProducer(props)
}

private fun createConsumer(config: Configuration.Kafka): Consumer<Unit, IBaseResource> {
    val props = Properties().apply {
        put("bootstrap.servers", config.host)
        put("group.id", config.groupId)
        put("key.deserializer", VoidDeserializer::class.java)
        put("value.deserializer", FhirResourceDeserializer::class.java)
    }
    return KafkaConsumer(props)
}
