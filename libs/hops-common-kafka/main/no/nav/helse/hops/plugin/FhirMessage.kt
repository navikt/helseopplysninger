package no.nav.helse.hops.plugin

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.UUID

class FhirMessage(val id: UUID, val content: ByteArray, val contentType: String) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.isNotBlank()) { "ContentType cannot be blank." }
    }
}

fun fromKafkaRecord(record: ConsumerRecord<UUID, ByteArray>) =
    FhirMessage(record.key(), record.value(), record.headers()["Content-Type"])
