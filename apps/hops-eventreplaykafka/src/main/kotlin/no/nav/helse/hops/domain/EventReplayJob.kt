package no.nav.helse.hops.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
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
        while (isActive) {
            try {
                val startingOffset = messageBus.sourceOffsetOfLatestMessage()
                eventStore.poll(startingOffset).collect { messageBus.publish(it) }
                isRunning = true
            } catch (ex: Throwable) {
                isRunning = false
                if (ex is CancellationException) throw ex
                logger.error("Error while publishing to message bus.", ex)
                delay(5000)
            }
        }
    }

    @Volatile
    var isRunning = true
        private set

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}

private fun EventStore.poll(startingOffset: Long) =
    flow {
        var offset = startingOffset

        while (true) {
            emitAll(search(offset).onEach { offset++ })
            delay(2000) // cancellable
        }
    }
