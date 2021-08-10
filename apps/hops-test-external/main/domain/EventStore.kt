package domain

import io.ktor.client.statement.HttpResponse

interface EventStore {
    suspend fun search(): HttpResponse
}
