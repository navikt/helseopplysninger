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
import no.nav.helse.hops.convert.ContentTypes
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val fhirFlow: KafkaFhirFlow,
    override val description: String = "publish fhir resource to make it available on kafka and eventstore",
    override var exception: Throwable? = null,
) : Test {
    private val sec5: Long = 5_000L

    override suspend fun test(): Boolean = runSuspendCatching {
        coroutineScope {
            val kafkaResponse = async { readTopic(sec5) }
            val apiResponse = async { api.post(FhirResource.generate()) }

            when (apiResponse.await().status) {
                HttpStatusCode.Accepted -> hasExpected(kafkaResponse)
                else -> cancelFlow(kafkaResponse)
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
    private suspend fun hasExpected(kafkaResponse: Deferred<FhirMessage?>) =
        when (kafkaResponse.await()) {
            null -> error("Message not available on kafka. Polled for ${sec5.toDuration(DurationUnit.MILLISECONDS)}")
            else -> true
        }

    private fun cancelFlow(kafkaResponse: Deferred<FhirMessage?>): Boolean {
        kafkaResponse.cancel("No need to wait any further")
        error("Failed to asynchronically produce expected record")
    }
}
