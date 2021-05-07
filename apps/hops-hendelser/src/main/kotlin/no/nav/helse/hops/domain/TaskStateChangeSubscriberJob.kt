package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.mapWith
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class TaskStateChangeSubscriberJob(
    taskChangeFeed: TaskChangeFeed,
    responseMapper: TaskChangeToMessageResponseMapper,
    messageBusProducer: MessageBusProducer,
    private val logger: Logger,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = taskChangeFeed
        .poll()
        .filter { it.current.status != it.previous?.status }
        .mapWith(responseMapper)
        .onEach { logger.info("Message: ${it.bundle.toJson()}") }
        .onEach { messageBusProducer.publish(it) }
        .catch { logger.error("Error while polling history.", it) }
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}
