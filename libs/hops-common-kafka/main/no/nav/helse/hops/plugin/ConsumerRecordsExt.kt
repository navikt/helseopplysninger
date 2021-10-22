package no.nav.helse.hops.plugin

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger

fun <K, V> Iterable<ConsumerRecord<K, V>>.logConsumed(
    logger: Logger,
): Iterable<ConsumerRecord<K, V>> = map { consumerRecord ->
    logger.trace("Consumed record with key ${consumerRecord.key()}")
    consumerRecord
}
