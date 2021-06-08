package no.nav.helse.hops.routing

import io.ktor.http.RequestConnectionPoint
import java.net.URL

fun RequestConnectionPoint.fullUrl() = URL(scheme, host, port, uri)
