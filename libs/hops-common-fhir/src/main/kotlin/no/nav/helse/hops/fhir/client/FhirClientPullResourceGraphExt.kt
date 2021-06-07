package no.nav.helse.hops.fhir.client

import no.nav.helse.hops.fhir.allChildren
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.UUID

/** Recursively pulls all the root's references resources and returns them as a list.
 * When pulling referenced resources the version that was current at the root's lastUpdated timestamp is selected. **/
suspend fun FhirClientReadOnly.pullResourceGraphSnapshot(root: Resource): List<Resource> {
    val at = root.meta.lastUpdated.toLocalDateTime()
    val resources = mutableMapOf<UUID, Resource>()

    suspend fun pullResourceGraphInner(res: Resource) =
        res.copy().apply { // defensive copy
            resources[idAsUUID()] = this
            allChildren<Reference>()
                .filter { it.hasReference() && !it.referenceElement.isAbsolute } // filter only relative references.
                .forEach {
                    val id = UUID.fromString(it.referenceElement.idPart)
                    if (!resources.containsKey(id)) {
                        val type = ResourceType.fromCode(it.referenceElement.resourceType)
                        val referencedResource = readHistory(type, id, at)
                        resources[id] = referencedResource
                    }
                }
        }

    pullResourceGraphInner(root)

    return resources.values.toList()
}
