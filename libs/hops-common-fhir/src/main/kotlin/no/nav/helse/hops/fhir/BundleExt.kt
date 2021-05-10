package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

/** For some reason the HAPI's json parser replaces all resource.id with entry.fullUrl,
 * this function returns a copy without this. **/
fun Bundle.withUuidPrefixFix(): Bundle =
    copy().apply {
        resources<Resource>().forEach {
            it.id = it.id?.removePrefix("urn:uuid:")
        }
    }

/** Returns resources from entries. **/
inline fun <reified R : Resource> Bundle.resources() =
    (entry ?: emptyList()).mapNotNull { it.resource as? R }

/** Add resources as entries. **/
fun Bundle.addResource(vararg res: Resource) =
    apply {
        res.map(::createEntry).forEach { addEntry(it) }
    }

private fun createEntry(res: Resource) =
    Bundle.BundleEntryComponent().apply {
        fullUrlElement = res.idElement.toUriType()
        resource = res
    }
