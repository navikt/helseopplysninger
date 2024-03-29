package eventreplay.domain

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
import kotlin.coroutines.CoroutineContext

class EventReplayJob(
    messageStream: FhirMessageStream,
    log: Logger,
    eventStore: EventStore,
    context: CoroutineContext = Dispatchers.Default
) : AutoCloseable {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            runCatching {
                val startingOffset = messageStream.sourceOffsetOfLatestMessage()
                eventStore.poll(startingOffset).collect(messageStream::publish)
                isRunning = true
            }.onFailure {
                isRunning = false
                if (it is CancellationException) throw it
                log.error("Error while publishing to message bus.", it)
                delay(5000)
            }
        }
    }

    @Volatile
    var isRunning = true
        private set

    override fun close() {
        if (!job.isCompleted) {
            runBlocking {
                job.cancelAndJoin()
            }
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
