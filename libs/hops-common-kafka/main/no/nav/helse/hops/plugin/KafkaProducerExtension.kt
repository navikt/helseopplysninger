package no.nav.helse.hops.plugin

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.Logger
import java.util.concurrent.Future

fun <K, V> Producer<K, V>.send(
    record: ProducerRecord<K, V>,
    callback: Callback,
    logger: Logger,
): Future<RecordMetadata> {
    logger.trace("Produced record with key ${record.key()}")
    return send(record, callback)
}
