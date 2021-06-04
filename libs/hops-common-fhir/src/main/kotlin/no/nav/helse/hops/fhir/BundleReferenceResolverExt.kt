package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource

private const val uuidPrefix = "urn:uuid:"

/** Returns a list of new resources where the ids and references have been resolved
 * according to https://www.hl7.org/fhir/bundle.html#references **/
fun Bundle.resourcesWithResolvedReferences() =
    copy().let {
        it.resolveIds()
        it.resolveReferences()
        resources<Resource>()
    }

private fun Bundle.resolveIds() =
    entry.forEach {
        if (it.fullUrl.startsWith(uuidPrefix))
            it.resource.id = it.fullUrl.removePrefix(uuidPrefix)
    }

private fun Bundle.resolveReferences() {
    val fullUrlToRefMap = entry.associate { it.fullUrl!! to it.resource.versionedReference() }

    resources<Resource>()
        .flatMap { res -> res.children().mapNotNull { it as? Reference } }
        .forEach { it.reference = fullUrlToRefMap.getOrDefault(it.reference, it.reference) }
}

private fun Resource.versionedReference() =
    "${resourceType}/${idElement.idPart}/_history/${meta.versionId ?: "1"}"
