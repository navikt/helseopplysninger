package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
import e2e.fhir.FhirContent
import e2e.fhir.FhirResource
import e2e.kafka.FhirMessage
import e2e.kafka.KafkaFhirFlow
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private const val sec25: Long = 25_000L
private val log = KotlinLogging.logger {}

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val kafka: KafkaFhirFlow,
) : Test {
    override val description: String = "publish fhir resource to make it available on kafka and eventstore"
    override var exception: Throwable? = null

    override suspend fun test(): Boolean = runSuspendCatching {
        kafka.seekToLatestOffset()
        val content = FhirResource.create()

        coroutineScope {
            val asyncKafkaResponse = consumeKafkaAsync(content)
            val asyncApiResponse = postResourceAsync(content)

            when (asyncApiResponse.await().status) {
                HttpStatusCode.Accepted -> asyncKafkaResponse.await().isNotNull
                else -> kafka.cancelFlow()
            }
        }
    }

    private fun CoroutineScope.postResourceAsync(content: FhirContent) = async(Dispatchers.IO) {
        api.post(content).also {
            log.trace("Posted content to API: $content")
        }
    }

    private fun CoroutineScope.consumeKafkaAsync(expectedContent: FhirContent) = async(Dispatchers.IO) {
        withTimeoutOrNull(sec25) {
            kafka.poll { record ->
                record.key() == expectedContent.id
            }.first { message: FhirMessage ->
                if (message.content != expectedContent.json) {
                    log.error { "Content was not equal" }
                    log.error { "Expected content: $expectedContent" }
                    log.error { "Actual content: $message" }
                }
                true
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private val FhirMessage?.isNotNull: Boolean
        get() = when (this) {
            null -> error("Message not available on kafka. Polled for ${sec25.toDuration(DurationUnit.MILLISECONDS)}")
            else -> true
        }
}
