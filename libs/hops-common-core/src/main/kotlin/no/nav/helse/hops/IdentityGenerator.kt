package no.nav.helse.hops

import com.fasterxml.uuid.Generators
import java.util.UUID

object IdentityGenerator {
    /** Used to create deterministic UUID. **/
    fun createUUID5(namespace: String, name: String) = createUUID5(UUID.fromString(namespace), name)

    /** Used to create deterministic UUID. **/
    fun createUUID5(namespace: UUID, name: String): UUID = Generators.nameBasedGenerator(namespace).generate(name)
}
