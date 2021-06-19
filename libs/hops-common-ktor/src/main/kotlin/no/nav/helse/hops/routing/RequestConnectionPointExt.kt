package no.nav.helse.hops.routing

import io.ktor.http.RequestConnectionPoint
import java.net.URL

fun RequestConnectionPoint.fullUrl() =
    if (port in listOf(80, 443)) URL(scheme, host, uri)
    else URL(scheme, host, port, uri)
