package no.nav.helse.hops.content

import io.ktor.http.ContentType

public val ContentType.Application.JsonFhirR4
    get() = ContentType("application", "json+fhir")
        .withParameter("fhirVersion", "4.0")
