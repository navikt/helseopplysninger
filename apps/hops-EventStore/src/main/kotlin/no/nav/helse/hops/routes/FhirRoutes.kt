package no.nav.helse.hops.routes

import ca.uhn.fhir.rest.api.Constants
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.routing.fullUrl
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.UrlType
import org.koin.ktor.ext.inject
import java.net.URI

fun Routing.fhirRoutes() {
    val searchService: FhirMessageSearchService by inject()
    val processService: FhirMessageProcessService by inject()

    authenticate {
        route("fhir/4.0") {
            get("/Bundle") {
                val count = call.countParam()
                val offset = call.offsetParam()
                val dst = call.destinationParam()

                val searchResult = searchService.search(count, offset, dst)

                if (searchResult.entry.count() == count)
                    searchResult.addLink(call.createNextLink())

                call.respond(searchResult)
            }

            /** Processes the message event synchronously according to
             * https://www.hl7.org/fhir/messageheader-operation-process-message.html **/
            post("/${Constants.EXTOP_PROCESS_MESSAGE}") {
                processService.process(call.receive())
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}

private const val SP_DST = "${Bundle.SP_MESSAGE}.${MessageHeader.SP_DESTINATION_URI}"
private fun ApplicationCall.destinationParam() = request.queryParameters[SP_DST]?.let { URI(it) }
private fun ApplicationCall.countParam() = request.queryParameters[Constants.PARAM_COUNT]?.toIntOrNull() ?: 10
private fun ApplicationCall.offsetParam() = request.queryParameters[Constants.PARAM_OFFSET]?.toLongOrNull() ?: 0

/** Creates a next-link, see: http://hl7.org/fhir/http.html#paging **/
private fun ApplicationCall.createNextLink(): Bundle.BundleLinkComponent {
    val count = countParam()
    val offset = offsetParam() + count
    val dst = destinationParam()

    var url = request.origin.fullUrl().toString().substringBefore('?')
    url = "$url?${Constants.PARAM_COUNT}=$count&${Constants.PARAM_OFFSET}=$offset"
    dst?.let { url = "$url&$SP_DST=$dst" }
    return Bundle.BundleLinkComponent(StringType(Bundle.LINK_NEXT), UrlType(url))
}
