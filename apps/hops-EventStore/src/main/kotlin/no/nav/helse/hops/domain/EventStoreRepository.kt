package no.nav.helse.hops.domain

import java.time.LocalDateTime
import java.util.UUID

interface EventStoreReadOnlyRepository {
    data class Query(
        val count: Int = 10,
        val offset: Long = 0,
        val destinationUri: String? = null,
        val messageId: UUID? = null
    )

    suspend fun search(query: Query): List<EventDto>
}

interface EventStoreRepository : EventStoreReadOnlyRepository {
    suspend fun add(event: EventDto)
}

class EventDto(
    val bundleId: UUID,
    val messageId: UUID,
    val requestId: String,
    val eventType: String,
    val bundleTimestamp: LocalDateTime,
    val recorded: LocalDateTime,
    val source: String,
    val destinations: List<String>,
    val data: ByteArray,
    val dataType: String
)
