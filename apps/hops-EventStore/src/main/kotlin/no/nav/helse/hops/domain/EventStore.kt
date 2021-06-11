package no.nav.helse.hops.domain

import java.time.LocalDateTime
import java.util.UUID

interface EventStoreReadOnly {
    data class Query(
        val offset: Int = 0,
        val count: Int = 10
    )

    suspend fun search(query: Query): List<EventDto>
}

interface EventStore : EventStoreReadOnly {
    suspend fun add(event: EventDto)
}

class EventDto(
    val bundleId: UUID,
    val messageId: UUID,
    val correlationId: String,
    val eventType: String,
    val recorded: LocalDateTime,
    val timestamp: LocalDateTime,
    val source: String,
    val destinations: List<String>,
    val data: ByteArray,
    val dataType: String,
)
