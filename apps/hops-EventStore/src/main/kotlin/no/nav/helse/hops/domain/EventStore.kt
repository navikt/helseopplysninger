package no.nav.helse.hops.domain

import java.time.OffsetDateTime
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
    val eventType: String,
    val recorded: OffsetDateTime,
    val timestamp: OffsetDateTime?,
    val source: String,
    val destinations: List<String>,
    val data: ByteArray,
    val dataType: String,
    val httpRequestHeaders: List<Pair<String, String>>
)
