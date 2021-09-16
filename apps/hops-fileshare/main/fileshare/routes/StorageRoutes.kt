package fileshare.routes

import fileshare.domain.FileSharingService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.origin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.request.contentType
import io.ktor.response.respond
import io.ktor.response.respondBytesWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.utils.io.copyAndClose
import no.nav.helse.hops.routing.fullUrl
import no.nav.helse.hops.security.HopsAuth
import no.nav.helse.hops.security.authIdentity

fun Routing.storageRoutes(service: FileSharingService) {
    route("files") {
        authenticate(HopsAuth.Realms.maskinportenWrite.name, HopsAuth.Realms.azureAd.name) {
            post {
                val fileName = service.uploadFile(call.request.receiveChannel(), call.request.contentType())

                val fileUrl = "${call.request.origin.fullUrl()}/$fileName"
                call.response.headers.append(HttpHeaders.Location, fileUrl)
                call.respond(HttpStatusCode.Created)
            }
        }
        authenticate(HopsAuth.Realms.maskinportenRead.name, HopsAuth.Realms.azureAd.name) {
            get("/{fileName}") {
                val fileName = call.parameters["fileName"]!!
                val identity = call.authIdentity()
                println(identity)

                val response = service.downloadFile(fileName, call.request.headers[HttpHeaders.Range])
                response.headers[HttpHeaders.AcceptRanges]?.let {
                    call.response.headers.append(HttpHeaders.AcceptRanges, it)
                }
                response.headers[HttpHeaders.LastModified]?.let {
                    call.response.headers.append(HttpHeaders.LastModified, it)
                }
                call.respondBytesWriter(response.contentType(), response.status) {
                    response.content.copyAndClose(this)
                }
            }
        }
    }
}
