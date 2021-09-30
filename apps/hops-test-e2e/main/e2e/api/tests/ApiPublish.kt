package e2e.api.tests

import e2e._common.Test
import e2e._common.TestUtils.retry
import e2e.api.ExternalApiFacade
import e2e.extension.hasContent
import e2e.fhir.FhirResource
import e2e.kafka.FhirKafkaListener
import e2e.kafka.KafkaConfig
import e2e.kafka.KafkaSubscription
import io.ktor.http.HttpStatusCode
import no.nav.helse.hops.convert.ContentTypes
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.UUID

internal class ApiPublish(
    override val name: String,
    private val kafkaConfig: KafkaConfig.Kafka,
    private val client: ExternalApiFacade,
    private val kafka: FhirKafkaListener,
    override val description: String = "publish fhir resource to make it available on kafka and eventstore",
    override var message: String? = null,
) : Test {
    override suspend fun test(): Boolean = runSuspendCatching {
        val subscription = kafka.subscribe(kafkaConfig.topic.published)

        val resource = FhirResource.generate()

        when (client.post(resource).status) {
            HttpStatusCode.Accepted -> isOnKafka(subscription) && isInEventstore()
            else -> false
        }
    }

    private fun isOnKafka(subscription: KafkaSubscription): Boolean {
        val anyMatch = subscription.use { kafka ->
            retry(100, 100L) {
                kafka.getMessages(kafkaConfig.topic.published)
                    .hasContent(::toFhirMessage) {
                        it.content == FhirResource.resource && it.contentType == ContentTypes.fhirJsonR4.toString()
                    }
            }
        }

        if (anyMatch.not()) message = "Wrong content-type or content in record on topic"
        return message == null
    }

    private fun isInEventstore(): Boolean = true
}

private data class FhirMessage(val contentType: String, val content: String)

private fun toFhirMessage(record: ConsumerRecord<UUID, ByteArray>) = FhirMessage(
    contentType = record.header(key = "Content-Type", defaultValue = ""),
    content = String(record.value())
)

private fun <K, V> ConsumerRecord<K, V>.header(key: String, defaultValue: String): String =
    headers().lastHeader(key)?.value()?.decodeToString() ?: defaultValue
