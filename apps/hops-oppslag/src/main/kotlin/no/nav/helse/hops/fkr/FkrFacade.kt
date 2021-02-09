package no.nav.helse.hops.fkr

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import no.nav.helse.hops.common.*

interface FkrFacade {
    suspend fun practitioner(hprNr: Int): String
}

class FkrFacadeImpl(
    private val baseUrl: String,
    private val oauth2Config: Oauth2ProviderConfig
    ) : FkrFacade {
    override suspend fun practitioner(hprNr: Int): String {
        HttpClient {
            install(Auth)
            {
                oauth2(oauth2Config)
            }
        }.use { client ->
            val response = client.get<String>("${baseUrl}/Hello")
            return response
        }
    }
}