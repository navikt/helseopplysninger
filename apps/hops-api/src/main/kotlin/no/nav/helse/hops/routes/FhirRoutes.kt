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
import no.nav.helse.hops.domain.HapiFacade
import no.nav.helse.hops.domain.add
import no.nav.helse.hops.toZonedDateTime
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Resource
import org.koin.ktor.ext.inject
import java.util.UUID

fun Routing.fhirRoutes() {
    route("/QuestionnaireResponse") {
        val hapi: HapiFacade by inject()

        get("/{id}") {
            val input = call.parameters["id"]
            val resource = hapi.read<QuestionnaireResponse>(input)

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
    respond(res)
    response.lastModified(res.meta.lastUpdated.toZonedDateTime())
    response.etag(res.weakEtag())
}

/** See https://www.hl7.org/fhir/http.html#create **/
private suspend fun ApplicationCall.createdResponse(res: Resource) {
    respond(HttpStatusCode.Created, res)
    response.header(HttpHeaders.Location, res.id)
    response.etag(res.weakEtag())
}

/** See https://www.hl7.org/fhir/http.html#versioning **/
private fun IBaseResource.weakEtag() = "W/\"${meta.versionId}\""

private suspend inline fun <reified R : Resource> HapiFacade.read(id: String?): R? {
    val logicalId = try { UUID.fromString(id) } catch (ex: IllegalArgumentException) { null } ?: return null
    return read(R::class, logicalId) as R?
}
