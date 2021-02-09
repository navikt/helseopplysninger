package no.nav.helse.hops.fkr

import io.ktor.client.*
import io.ktor.client.request.*

interface FkrFacade {
    suspend fun practitioner(hprNr: Int): String
}

class FkrFacadeImpl(
    private val baseUrl: String,
    private val client: HttpClient
    ) : FkrFacade {
    override suspend fun practitioner(hprNr: Int): String {
        val response = client.get<String>("${baseUrl}/Hello")
        return response
    }
}