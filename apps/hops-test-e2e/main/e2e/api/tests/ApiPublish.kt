package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
import e2e.fhir.FhirResource
import e2e.kafka.FhirMessage
import e2e.kafka.KafkaFhirFlow
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import no.nav.helse.hops.convert.ContentTypes
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private val log = KotlinLogging.logger {}

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val fhirFlow: KafkaFhirFlow,
) : Test {
    override val description: String = "publish fhir resource to make it available on kafka and eventstore"
    override var exception: Throwable? = null

    override suspend fun test(): Boolean = runSuspendCatching {
        coroutineScope {
            val asyncKafkaResponse = async {
                readTopic(sec25)
            }

            val asyncApiResponse = async {
                api.post(FhirResource.generate()).also {
                    log.trace("Sent record with key ${FhirResource.id} to API.")
                }
            }

            when (asyncApiResponse.await().status) {
                HttpStatusCode.Accepted -> hasExpected(asyncKafkaResponse.await())
                else -> cancelFlow(asyncKafkaResponse)
            }
        }
    }

    private suspend fun readTopic(timeout: Long) = withTimeoutOrNull(timeout) {
        fhirFlow.poll().firstOrNull { record ->
            val expectedResource = FhirResource.resource
            val expectedType = ContentTypes.fhirJsonR4.toString()
            record.content == expectedResource && record.contentType == expectedType
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun hasExpected(fhirMessage: FhirMessage?) =
        when (fhirMessage) {
            null -> error("Message not available on kafka. Polled for ${sec25.toDuration(DurationUnit.MILLISECONDS)}")
            else -> true
        }

    private fun cancelFlow(kafkaResponse: Deferred<FhirMessage?>): Boolean {
        kafkaResponse.cancel("No need to wait any further")
        error("Failed to asynchronically produce expected record")
    }

    private val sec25: Long = 25_000L
}
