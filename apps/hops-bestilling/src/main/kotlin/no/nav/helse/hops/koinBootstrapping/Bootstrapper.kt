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
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.Properties

object Bootstrapper {
    val koinModule = module {
        singleClosable<Producer<Unit, IBaseResource>> {
            val host = getProperty("kafka.host")
            val props = Properties().apply {
                put("bootstrap.servers", host)
                put("key.serializer", VoidSerializer::class.java)
                put("value.serializer", FhirResourceSerializer::class.java)
            }
            KafkaProducer(props)
        }

        singleClosable<Consumer<Unit, IBaseResource>> {
            val host = getProperty("kafka.host")
            val groupId = getProperty("kafka.group_id")
            val props = Properties().apply {
                put("bootstrap.servers", host)
                put("group.id", groupId)
                put("key.deserializer", VoidDeserializer::class.java)
                put("value.deserializer", FhirResourceDeserializer::class.java)
            }
            KafkaConsumer(props)
        }

        single { Service(get(), get(), LoggerFactory.getLogger(Service::class.java)) }
    }
}

/** Register Closeable as singleton and tie its lifetime to the Module. **/
private inline fun <reified T : Closeable> org.koin.core.module.Module.singleClosable(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = single(qualifier, createdAtStart, override, definition).onClose { it?.close() }
