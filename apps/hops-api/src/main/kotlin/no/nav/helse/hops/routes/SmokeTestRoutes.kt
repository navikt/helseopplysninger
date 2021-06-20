package no.nav.helse.hops.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.domain.EventStore
import org.koin.ktor.ext.inject
import java.net.URL

fun Routing.smokeTestRoutes() {
    route("/smokeTests") {
        val eventStore by inject<EventStore>()

        get("/eventStore") {
            try {
                val response = eventStore.search(
                    URL("https://smoke.test?_count=1"),
                    ContentTypes.fhirJsonR4,
                    "smoke-test-by-api"
                )

                if (response.status.isSuccess()) call.respondText { "OK!" }
                else call.respond(
                    HttpStatusCode.InternalServerError,
                    "Received statusCode=${response.status} from EventStore."
                )
            } catch (ex: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ex.message ?: "No exception message."
                )
            }
        }
    }
}
