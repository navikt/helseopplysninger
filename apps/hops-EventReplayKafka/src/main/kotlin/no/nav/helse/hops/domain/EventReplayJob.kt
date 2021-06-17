package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class EventReplayJob(
    messageBus: FhirMessageBus,
    logger: Logger,
    eventStore: EventStore,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = CoroutineScope(context).launch {
        eventStore
            .poll(messageBus.sourceOffsetOfLatestMessage())
            .onEach { messageBus.publish(it) }
            .catch { logger.error("Error while publishing to message bus.", it) } // TODO: Should retry.
            .collect()
    }

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}
