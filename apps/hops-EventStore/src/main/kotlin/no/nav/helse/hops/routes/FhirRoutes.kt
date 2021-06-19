package no.nav.helse.hops.routes

import ca.uhn.fhir.rest.api.Constants
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.helse.hops.domain.FhirMessageProcessService
import no.nav.helse.hops.domain.FhirMessageSearchService
import no.nav.helse.hops.domain.FhirValidatorFactory
import no.nav.helse.hops.fhir.JsonConverter
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
        route("fhir") {
            get("/Bundle") {
                val rcv = call.destinationParam()
                val count = call.countParam() ?: 10
                val offset = call.offsetParam() ?: 0

                val searchSet = searchService.search(count, offset, rcv)

                if (searchSet.entry.count() == count) {
                    var url = call.request.local.fullUrl().toString().substringBefore('?')
                    url = "$url?${Constants.PARAM_COUNT}=$count&${Constants.PARAM_OFFSET}=${offset + count}"
                    url = if (rcv != null) "$url&$SP_RCV=$rcv" else url

                    val nextLink = Bundle.BundleLinkComponent(StringType(Bundle.LINK_NEXT), UrlType(url))
                    searchSet.addLink(nextLink)
                }

                call.respond(searchSet)
            }

            /** Processes the message event synchronously according to
             * https://www.hl7.org/fhir/messageheader-operation-process-message.html **/
            post("/${Constants.EXTOP_PROCESS_MESSAGE}") {

                // TODO: Cannot use receive because it uses converter due to bug in Ktor: https://youtrack.jetbrains.com/issue/KTOR-2189
                // val message: Bundle = call.receive()
                val message: Bundle = JsonConverter.parse(call.receiveText())

                processService.process(message)
                call.respond(HttpStatusCode.Accepted)
            }

            install(FhirValidatorKtorPlugin.Feature) {
                validator = FhirValidatorFactory.relaxedR4
            }
        }
    }
}

private const val SP_RCV = "${Bundle.SP_MESSAGE}.${MessageHeader.SP_DESTINATION_URI}"
private fun ApplicationCall.destinationParam() = request.queryParameters[SP_RCV]?.let { URI(it) }
private fun ApplicationCall.countParam() = request.queryParameters[Constants.PARAM_COUNT]?.toIntOrNull()
private fun ApplicationCall.offsetParam() = request.queryParameters[Constants.PARAM_OFFSET]?.toLongOrNull()
