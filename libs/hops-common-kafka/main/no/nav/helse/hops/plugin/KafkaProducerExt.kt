package no.nav.helse.hops.plugin

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.Logger
import java.util.concurrent.Future
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun <K, V> Producer<K, V>.send(
    record: ProducerRecord<K, V>,
    callback: Callback,
    logger: Logger,
): Future<RecordMetadata> {
    logger.trace("Produced record with key ${record.key()}")
    return send(record, callback)
}

/** Same as [Producer.send], but implemented as a Kotlin suspend function. **/
suspend fun <K, V> Producer<K, V>.sendAwait(record: ProducerRecord<K, V>) {
    suspendCoroutine<RecordMetadata> { continuation ->
        val callback = Callback { metadata, exception ->
            if (metadata == null) continuation.resumeWithException(exception!!)
            else continuation.resume(metadata)
        }

        send(record, callback)
    }
}
