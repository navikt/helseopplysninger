package eventsink.domain

import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode
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
    private val logger: Logger,
    private val eventStore: EventStore,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            try {
                messageBus.poll().collect(::addToEventStore)
                isRunning = true
            } catch (ex: Throwable) {
                isRunning = false
                if (ex is CancellationException) throw ex
                logger.error("Error while publishing to EventStore.", ex)
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

    private suspend fun addToEventStore(message: FhirMessage) =
        try {
            eventStore.add(message)
        } catch (ex: ClientRequestException) {
            if (ex.response.status in listOf(HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity))
                logger.error("The FHIR Message is invalid and will be ignored.", ex)
            else throw ex
        }
}
