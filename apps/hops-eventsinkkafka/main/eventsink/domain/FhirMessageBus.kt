package eventsink.domain

import kotlinx.coroutines.flow.Flow

interface FhirMessageBus {
    fun poll(): Flow<FhirMessage>
}
