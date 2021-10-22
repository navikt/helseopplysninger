package no.nav.helse.hops.plugin

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.UUID

class FhirMessage(val id: UUID, val content: ByteArray, val contentType: String) {
    init {
        require(content.isNotEmpty()) { "Content cannot be empty." }
        require(contentType.isNotBlank()) { "ContentType cannot be blank." }
    }
}

fun fromKafkaRecord(record: ConsumerRecord<UUID, ByteArray>): FhirMessage {
    fun valueOf(header: String) = record.headers().lastHeader(header).value().decodeToString()
    val contentType = valueOf("Content-Type")

    return FhirMessage(record.key(), record.value(), contentType)
}
