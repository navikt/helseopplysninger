package eventreplay.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext

class EventReplayJob(
    messageBus: FhirMessageStream,
    log: Logger,
    eventStore: EventStore,
    context: CoroutineContext
) {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            runCatching {
                val startingOffset = messageBus.sourceOffsetOfLatestMessage()
                eventStore.poll(startingOffset).collect(messageBus::publish)
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
}

private fun EventStore.poll(startingOffset: Long) =
    flow {
        var offset = startingOffset

        while (true) {
            emitAll(search(offset).onEach { offset++ })
            delay(2000) // cancellable
        }
    }
