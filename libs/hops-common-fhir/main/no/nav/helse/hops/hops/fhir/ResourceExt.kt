package no.nav.helse.hops.hops.fhir

import org.hl7.fhir.r4.model.Base
import org.hl7.fhir.r4.model.Resource
import java.util.UUID
import kotlin.reflect.KClass

/** See https://www.hl7.org/fhir/http.html#versioning **/
fun Resource.weakEtag() = "W/\"${meta?.versionId ?: "0"}\""

/** IdPart as UUID. **/
fun Resource.idAsUUID() = UUID.fromString(idElement.idPart)!!

/** Recursively iterate over all child-elements of given type. **/
inline fun <reified T : Base> Base.allChildren() =
    allChildren(T::class).map { it as T }

fun <T : Base> Base.allChildren(type: KClass<T>): Sequence<Base> {
    return sequence {
        for (child in children().flatMap { it.values }) {
            if (child.javaClass == type.java)
                yield(child)
            else
                yieldAll(child.allChildren(type))
        }
    }
}
