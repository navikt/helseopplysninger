package no.nav.helse.hops.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class EventSinkJob(
    messageBus: FhirMessageBus,
    logger: Logger,
    eventStore: EventStore,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            try {
                messageBus.poll().collect { eventStore.add(it) }
            }
            catch (ex: Throwable) {
                if (ex is CancellationException) throw ex
                logger.error("Error while publishing to message bus.", ex)
                delay(5000)
            }
        }
    }

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}
