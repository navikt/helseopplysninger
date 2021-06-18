package no.nav.helse.hops.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.client.statement.HttpResponse
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.request.queryString
import io.ktor.response.header
import io.ktor.response.respondBytesWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.utils.io.copyAndClose
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.domain.EventStore
import org.koin.ktor.ext.inject

fun Routing.fhirRoutes() {
    authenticate {
        route("fhir/4.0") {
            val eventStore: EventStore by inject()
            val ct = ContentTypes.fhirJsonR4

            get("/Bundle") {
                val response = eventStore.search(call.request.queryString(), ct, call.callId!!)
                call.proxyDownstream(response)
            }

            post("/\$process-message") {
                val response = eventStore.publish(call.request.receiveChannel(), ct, call.callId!!)
                call.proxyDownstream(response)
            }
        }
    }
}

/** Forward the content of the upstream-response (from EventStore) to the downstream client. **/
private suspend fun ApplicationCall.proxyDownstream(upstreamResponse: HttpResponse) {
    response.header(HttpHeaders.XRequestId, callId!!)
    respondBytesWriter(upstreamResponse.contentType(), upstreamResponse.status) {
        upstreamResponse.content.copyAndClose(this)
    }
}
