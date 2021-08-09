package no.nav.helse.hops.hops.utils

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.DataFormatException
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.util.Properties

object KafkaFactory {
    fun createFhirProducer(config: DockerComposeEnv = DockerComposeEnv()): Producer<Unit, IBaseResource> {
        val props = createCommonKafkaProperties(config).also {
            it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = VoidSerializer::class.java
            it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaFhirResourceSerializer::class.java
            it[ProducerConfig.ACKS_CONFIG] = "all"
            it[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "true"
        }
        return KafkaProducer(props)
    }

    fun createFhirConsumer(config: DockerComposeEnv = DockerComposeEnv()): Consumer<Unit, IBaseResource> {
        val props = createCommonKafkaProperties(config).also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = "autotest"
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = VoidDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaFhirResourceDeserializer::class.java
        }
        return KafkaConsumer(props)
    }

    private fun createCommonKafkaProperties(config: DockerComposeEnv): Properties {
        return Properties().also {
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = config.kafkaBrokers
            it[CommonClientConfigs.CLIENT_ID_CONFIG] = "autotest"
        }
    }
}

class KafkaFhirResourceDeserializer : Deserializer<IBaseResource> {
    override fun deserialize(topic: String?, data: ByteArray?): IBaseResource? {
        ByteArrayInputStream(data!!).use {
            val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)!!
            val jsonParser = fhirContext.newJsonParser()!!

            return try {
                jsonParser.parseResource(it)
            } catch (_: DataFormatException) {
                null // Unparsable FHIR Json will be silently ignored.
            }
        }
    }
}

class KafkaFhirResourceSerializer : Serializer<IBaseResource> {
    override fun serialize(topic: String?, data: IBaseResource?): ByteArray {
        ByteArrayOutputStream().use { stream ->
            OutputStreamWriter(stream).use { writer ->
                val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)!!
                val jsonParser = fhirContext.newJsonParser()!!
                jsonParser.encodeResourceToWriter(data!!, writer)
            }

            return stream.toByteArray()
        }
    }
}
