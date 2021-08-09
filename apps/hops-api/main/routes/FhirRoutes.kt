package routes

import domain.EventStore
import infrastructure.Constants
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.client.statement.HttpResponse
import io.ktor.features.callId
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.request.accept
import io.ktor.request.contentType
import io.ktor.response.respond
import io.ktor.response.respondBytesWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.utils.io.copyAndClose
import no.nav.helse.hops.routing.fullUrl
import org.koin.ktor.ext.inject

fun Routing.fhirRoutes() {
    route("fhir/4.0") {
        val eventStore: EventStore by inject()

        authenticate(Constants.SUBSCRIBE) {
            get("/Bundle") {
                val response = eventStore.search(
                    call.request.origin.fullUrl(),
                    call.request.accept()?.let { ContentType.parse(it) } ?: ContentType.Any,
                    call.callId!!
                )

                call.proxyDownstream(response)
            }
        }

        authenticate(Constants.PUBLISH) {
            post("/\$process-message") {
                val response = eventStore.publish(
                    call.request.origin.fullUrl(),
                    call.request.receiveChannel(),
                    call.request.contentType(),
                    call.callId!!
                )

                call.proxyDownstream(response)
            }
        }
    }
}

/** Forward the content of the upstream-response (from EventStore) to the downstream client. **/
private suspend fun ApplicationCall.proxyDownstream(upstreamResponse: HttpResponse) {
    if ((upstreamResponse.contentLength() ?: 0) <= 0) {
        respond(upstreamResponse.status)
    } else {
        respondBytesWriter(upstreamResponse.contentType(), upstreamResponse.status) {
            upstreamResponse.content.copyAndClose(this)
        }
    }
}
