package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.fhir.allByQuery
import no.nav.helse.hops.fhir.allByUrl
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.infrastructure.Configuration
import no.nav.helse.hops.mapWith
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Task
import org.slf4j.Logger
import java.io.Closeable
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

class TaskStateChangeSubscriberJob(
    taskChangeFeed: TaskChangeFeed,
    responseMapper: TaskChangeToMessageResponseMapper,
    private val config: Configuration.FhirMessaging,
    private val messageBusProducer: MessageBusProducer,
    private val fhirClient: IGenericClient,
    private val logger: Logger,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = taskChangeFeed
        .poll(lastUpdatedFromTaskInMostRecentPublishedMessageResponse())
        .filter { it.current.status != it.previous?.status }
        .mapWith(responseMapper)
        .mapNotNull { it }
        .onEach { process(it) }
        .catch { logger.error("Error while polling history.", it) }
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    /** We use this timestamp as a starting point from where to continue
     * polling changes to be published on the kafka topic. **/
    private fun lastUpdatedFromTaskInMostRecentPublishedMessageResponse(): LocalDateTime {
        val result = fhirClient
            .allByUrl("MessageHeader?source-uri=${config.endpoint}&_include=MessageHeader:focus:Task&_sort=-_lastUpdated&_count=1")
            .filterIsInstance<Task>()
            .singleOrNull() ?: return LocalDateTime.MIN

        return result.meta.lastUpdated.toLocalDateTime()
    }

    private suspend fun process(msg: OkResponseMessage) {
        logger.info("Message: ${msg.bundle.toJson()}")

        // Check if Response-Message exists in hapi, in that case it has already been published to kafka.
        val msgResponseExists = fhirClient
            .allByQuery<MessageHeader>("_id=${msg.header.idElement.idPart}")
            .any()

        if (!msgResponseExists) {
            messageBusProducer.publish(msg)
            fhirClient.update().resource(msg.header).execute()
        }
    }
}
