package no.nav.helse.hops.hops.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val client = HttpClient()

fun urlReturnsStatusCode(url: Url, code: Int): Boolean {
    return try {
        val response: HttpResponse = runBlocking {
            client.request(url)
        }
        response.status.value == code
    } catch (e: Exception) {
        LoggerFactory.getLogger("Autotest").warn("urlReturnsStatusCode failed: " + e.message)
        false
    }
}
