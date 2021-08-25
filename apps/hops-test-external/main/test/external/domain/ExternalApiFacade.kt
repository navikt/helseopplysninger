package test.external.domain

import io.ktor.client.statement.HttpResponse

interface ExternalApiFacade {
    suspend fun get(): HttpResponse
}
