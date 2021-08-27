package no.nav.helse.hops.utils

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.serialization.Serializable

@Serializable
data class SutSystem(
    val name: String,
    val port: Int,
    val readyPath: String,
    var ready: Boolean? = null,
)

fun getSutServices(): Array<SutSystem> {
    return arrayOf(
        // Dette burde jeg hentet inn dynamic fra naiserator.yaml-filene.
        SutSystem("hops-hapi-fhir-server", 8084, "/actuator/health/readiness"),
        SutSystem("hops-bestilling", 8085, "/isReady"),
        SutSystem("hops-api", 8082, "/isReady"),
    )
}

fun checkSutServices(): Array<SutSystem> {
    val out = mutableListOf<SutSystem>()
    getSutServices().forEach {
        val url = URLBuilder()
        url.host = "0.0.0.0"
        url.port = it.port
        url.protocol = URLProtocol.HTTP
        url.encodedPath = it.readyPath
        it.ready = urlReturnsStatusCode(url.build(), 200)
        out.add(it)
    }

    return out.toTypedArray()
}
