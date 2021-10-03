package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
import e2e.fhir.FhirResource
import e2e.kafka.FhirKafkaListener
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import no.nav.helse.hops.convert.ContentTypes

internal class ApiPublish(
    override val name: String,
    private val api: ExternalApiFacade,
    private val flow: FhirKafkaListener,
    override val description: String = "publish fhir resource to make it available on kafka and eventstore",
    override var exception: Throwable? = null,
) : Test {
    private val sec5 = 5_000L

    override suspend fun test(): Boolean = runSuspendCatching {
        coroutineScope {
            val kafkaResponse = async { kafkaListener() }
            val apiResponse = async { api.post(FhirResource.generate()) }

            when (apiResponse.await().status) {
                HttpStatusCode.Accepted -> kafkaResponse.await() != null
                else -> false.also { kafkaResponse.cancel("Failed to produce expected record (async)") }
            }
        }
    }

    private suspend fun kafkaListener() = withTimeoutOrNull(sec5) {
        flow.poll().firstOrNull { record ->
            val expectedResource = FhirResource.resource
            val expectedType = ContentTypes.fhirJsonR4.toString()
            record.content == expectedResource && record.contentType == expectedType
        }
    }
}
