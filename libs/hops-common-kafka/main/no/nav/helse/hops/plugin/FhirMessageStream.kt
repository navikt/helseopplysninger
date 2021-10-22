package no.nav.helse.hops.plugin

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration
import java.util.UUID

interface FhirMessageStream {
    fun <R> poll(map: (ConsumerRecord<UUID, ByteArray>) -> R?): Flow<R>
}

class FhirMessageStreamKafka(
    private val consumer: Consumer<UUID, ByteArray>,
    private val topic: String,
    private val timeout: Duration = Duration.ofSeconds(2)
) : FhirMessageStream {
    override fun <R> poll(map: (ConsumerRecord<UUID, ByteArray>) -> R?): Flow<R> =
        flow {
            try {
                consumer.subscribe(listOf(topic))

                while (currentCoroutineContext().isActive) {
                    val records = consumer.poll(timeout)
                    records.mapNotNull(map).forEach { emit(it) }
                }
            } finally {
                consumer.unsubscribe()
            }
        }
}
