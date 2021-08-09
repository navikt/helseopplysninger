package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.MessageHeader

val MessageHeader.fullyQualifiedEventType: String get() =
    if (hasEventUriType()) eventUriType.value
    else eventCoding.run {
        require(hasSystem() || hasCode()) { "Both 'System' and 'Code' cannot be empty." }
        var coding = "$system|$code"
        if (hasVersion()) coding += "|$version"
        return coding
    }
