package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.mapWith
import org.hl7.fhir.r4.model.MessageHeader
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class TaskStateChangeSubscriberJob(
    taskChangeFeed: TaskChangeFeed,
    responseMapper: TaskChangeToMessageResponseMapper,
    private val messageBusProducer: MessageBusProducer,
    private val fhirClient: IGenericClient,
    private val logger: Logger,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = taskChangeFeed
        .poll()
        .filter { it.current.status != it.previous?.status }
        .mapWith(responseMapper)
        .onEach { process(it) }
        .catch { logger.error("Error while polling history.", it) }
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    private suspend fun process(msg: OkResponseMessage) {
        logger.info("Message: ${msg.bundle.toJson()}")

        try {
            fhirClient
                .read()
                .resource(MessageHeader::class.java)
                .withId(msg.header.id)
                .execute()
        } catch (ex: ResourceNotFoundException) {
            messageBusProducer.publish(msg)
            fhirClient.update().resource(msg.header).execute()
        }
    }
}