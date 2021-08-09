package no.nav.helse.hops.diagnostics

import io.ktor.features.CallId
import io.ktor.features.RejectedCallIdException
import io.ktor.http.HttpHeaders
import java.util.UUID

fun CallId.Configuration.useRequestIdHeader(maxLength: Int = 200) {
    header(HttpHeaders.XRequestId)
    generate { UUID.randomUUID().toString() }
    verify { if (it.length > maxLength) throw RejectedCallIdException(it) else it.isNotBlank() }
}
