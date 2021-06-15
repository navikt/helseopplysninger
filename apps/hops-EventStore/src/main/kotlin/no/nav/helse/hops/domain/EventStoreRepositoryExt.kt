package no.nav.helse.hops.domain

import java.util.UUID

suspend fun EventStoreReadOnlyRepository.ensureExists(messageId: UUID) {
    val query = EventStoreReadOnlyRepository.Query(messageId = messageId)
    val result = search(query)
    check(result.count() == 1) { "Message with ID=$messageId does not exist." }
}
