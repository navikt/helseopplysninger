package no.nav.helse.hops.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class StateChangeNotificationsJob(
    historyFeed: FhirHistoryFeed,
    private val logger: Logger,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = historyFeed
        .poll()
        .onEach(::process)
        .catch { logger.error("Error while polling history.", it) }
        .launchIn(CoroutineScope(context))

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    private suspend fun process(resource: Resource) {
        logger.info("Message: ${resource.toJson()}")
    }
}
