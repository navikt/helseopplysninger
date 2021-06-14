package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class MessageConsumerJob(
    messageBus: FhirMessageBus,
    logger: Logger,
    eventStore: EventStore,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = messageBus
        .poll()
        .onEach { eventStore.add(it) }
        .catch { logger.error("Error while polling message bus.", it) } // TODO: Should retry and\or put on DLQ.
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}
