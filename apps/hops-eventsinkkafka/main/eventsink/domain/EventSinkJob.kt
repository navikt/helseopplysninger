package eventsink.domain

import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.helse.hops.plugin.FhirMessage
import no.nav.helse.hops.plugin.MessageStream
import no.nav.helse.hops.plugin.fromKafkaRecord
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext

class EventSinkJob(
    messageStream: MessageStream,
    private val logger: Logger,
    private val eventStore: EventStore,
    context: CoroutineContext
) {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            try {
                messageStream.poll(::fromKafkaRecord).collect(::addToEventStore)
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

    private suspend fun addToEventStore(message: FhirMessage) =
        try {
            eventStore.add(message)
        } catch (ex: ClientRequestException) {
            if (ex.response.status in listOf(HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity))
                logger.error("The FHIR Message is invalid and will be ignored.", ex)
            else throw ex
        }
}
