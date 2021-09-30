package e2e.extension

import org.apache.kafka.clients.consumer.ConsumerRecord

fun <K, V, C> List<ConsumerRecord<K, V>>.hasContent(
    converter: (record: ConsumerRecord<K, V>) -> C,
    content: (C) -> Boolean,
): Boolean = map(converter).any(content)
