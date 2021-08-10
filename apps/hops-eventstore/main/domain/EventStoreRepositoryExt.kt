package domain

import java.util.UUID

suspend fun EventStoreReadOnlyRepository.ensureExists(messageId: UUID) {
    checkNotNull(getByIdOrNull(messageId)) { "Message with ID=$messageId does not exist." }
}

suspend fun EventStoreReadOnlyRepository.getByIdOrNull(messageId: UUID) =
    search(EventStoreReadOnlyRepository.Query(messageId = messageId)).singleOrNull()
