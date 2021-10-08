package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
import e2e.fhir.FhirResource
import e2e.kafka.FhirMessage
import e2e.kafka.KafkaFhirFlow
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import no.nav.helse.hops.convert.ContentTypes
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private val log = KotlinLogging.logger {}
private const val sec1 = 1_000L
private const val sec25: Long = 25_000L

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val kafka: KafkaFhirFlow,
) : Test, CoroutineScope {
    override val description: String = "publish fhir resource to make it available on kafka and eventstore"
    override var exception: Throwable? = null

    private val job = CoroutineScope(Dispatchers.Default).launch {
        while (isActive) runCatching {
            kafka.poll()
        }.onFailure {
            log.error("Error while reading topic", it)
            if (it is CancellationException) throw it
            delay(sec1)
        }
    }

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override suspend fun test(): Boolean = runSuspendCatching {
        kafka.seekToLatestOffset()
        val resource = FhirResource.create()

        coroutineScope {
            val asyncKafkaResponse = async(Dispatchers.IO) { readTopic(resource.id) }
            val asyncApiResponse = async(Dispatchers.IO) { api.post(resource.id, resource.content) }

            when (asyncApiResponse.await().status) {
                HttpStatusCode.Accepted -> hasExpected(asyncKafkaResponse.await())
                else -> cancelFlow()
            }
        }
    }

    private suspend fun readTopic(id: UUID): FhirMessage? =
        withTimeoutOrNull(sec25) {
            val expectedResource = FhirResource.get { it.id == id }.firstOrNull() ?: error("resources was not in cache")
            val expectedType = ContentTypes.fhirJsonR4.toString()
            kafka.poll().first { it.content == expectedResource.content && it.contentType == expectedType }
        }

    @OptIn(ExperimentalTime::class)
    private fun hasExpected(fhirMessage: FhirMessage?) =
        when (fhirMessage) {
            null -> error("Message not available on kafka. Polled for ${sec25.toDuration(DurationUnit.MILLISECONDS)}")
            else -> true
        }

    private fun cancelFlow(): Boolean {
        job.cancel() // FIXME: what happens after this state? Will the app recover without restart
        error("Failed to asynchronically produce expected record")
    }
}
