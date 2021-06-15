package no.nav.helse.hops.fhir

import ca.uhn.fhir.rest.api.Constants.HEADER_REQUEST_ID
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.StringType

/** Property extension to set\get x-request-id as http-response-header extension.
 * See http://hl7.org/fhir/StructureDefinition/http-response-header **/
var Bundle.BundleEntryComponent.requestId: String
    get() = response.extension.single(::isRequestHeader).primitiveValue().substringAfter(':').trim()
    set(value) {
        response = response ?: Bundle.BundleEntryResponseComponent(StringType("201"))
        response.apply {
            extension.removeIf(::isRequestHeader)
            addExtension(HTTP_RESPONSE_HEADER_EXTENSION_URL, StringType("$HEADER_REQUEST_ID: $value"))
        }
    }

private const val HTTP_RESPONSE_HEADER_EXTENSION_URL = "http://hl7.org/fhir/StructureDefinition/http-response-header"

private fun isRequestHeader(ext: Extension) =
    ext.url == HTTP_RESPONSE_HEADER_EXTENSION_URL && ext.primitiveValue().startsWith(HEADER_REQUEST_ID)
