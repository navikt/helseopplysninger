package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
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
import no.nav.helse.hops.convert.ContentTypes
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
        val resource = FhirResource.create()

        coroutineScope {
            val asyncKafkaResponse = consumeKafkaAsync(resource)
            val asyncApiResponse = postResourceAsync(resource)

            when (asyncApiResponse.await().status) {
                HttpStatusCode.Accepted -> asyncKafkaResponse.await().isNotNull
                else -> kafka.cancelFlow()
            }
        }
    }

    private fun CoroutineScope.postResourceAsync(resource: FhirResource.Resource) = async(Dispatchers.IO) {
        api.post(resource.content).also {
            log.trace("Post resource with ID ${resource.id} to API.")
        }
    }

    private fun CoroutineScope.consumeKafkaAsync(resource: FhirResource.Resource) = async(Dispatchers.IO) {
        withTimeoutOrNull(sec25) {
            val expectedType = ContentTypes.fhirJsonR4.toString()
            val expectedResource = FhirResource.get { it.id == resource.id }.single()

            kafka.poll().first { consumedResource ->
                consumedResource.content == expectedResource.content && consumedResource.contentType == expectedType
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
