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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private const val sec25: Long = 25_000L
private val log = KotlinLogging.logger {}

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val kafka: KafkaFhirFlow,
    private val context: CoroutineContext
) : Test {
    override val description: String = "publish fhir resource to make it available on kafka and eventstore"
    override var exception: Throwable? = null

    override suspend fun test(): Boolean = runSuspendCatching {
        kafka.seekToLatestOffset()
        val content = FhirResource.create()

        withContext(context) {
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

    private fun CoroutineScope.consumeKafkaAsync(publishedMsg: FhirContent) = async(Dispatchers.IO) {
        withTimeoutOrNull(sec25) {
            kafka.poll { record ->
                record.key() == publishedMsg.id
            }.first { consumedMsg: FhirMessage ->
                val actualContent = consumedMsg.content.removeWhitespaces()
                val expectedContent = consumedMsg.content.removeWhitespaces()
                if (actualContent != expectedContent) {
                    log.error { "Content was not equal" }
                    log.error { "Expected content: $publishedMsg" }
                    log.error { "Actual content: $consumedMsg" }
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

private fun String.removeWhitespaces() = replace("\\s".toRegex(), "")
