package no.nav.helse.hops.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.etag
import io.ktor.response.header
import io.ktor.response.lastModified
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.add
import no.nav.helse.hops.fhir.client.readOrNull
import no.nav.helse.hops.fhir.weakEtag
import no.nav.helse.hops.toZonedDateTime
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Resource
import org.koin.ktor.ext.inject
import java.util.UUID

fun Routing.fhirRoutes() {
    route("/QuestionnaireResponse") {
        val hapi: FhirClient by inject()

        get("/{id}") {
            val input = call.parameters["id"]
            val resource: QuestionnaireResponse? = hapi.run {
                val id =
                    try { UUID.fromString(input) } catch (ex: IllegalArgumentException) { null } ?: return@run null
                return@run readOrNull(id)
            }

            if (resource != null)
                call.readResponse(resource)
            else
                call.respond(HttpStatusCode.NotFound)
        }

        post {
            val inputQr = call.receive<QuestionnaireResponse>()
            val createdQr = hapi.add(inputQr)
            call.createdResponse(createdQr)
        }
    }
}

/** See https://www.hl7.org/fhir/http.html#read **/
private suspend fun ApplicationCall.readResponse(res: Resource) {
    response.lastModified(res.meta.lastUpdated.toZonedDateTime())
    response.etag(res.weakEtag())
    respond(res)
}

/** See https://www.hl7.org/fhir/http.html#create **/
private suspend fun ApplicationCall.createdResponse(res: Resource) {
    response.header(HttpHeaders.Location, "/${res.id}")
    response.etag(res.weakEtag())
    respond(HttpStatusCode.Created, res)
}
