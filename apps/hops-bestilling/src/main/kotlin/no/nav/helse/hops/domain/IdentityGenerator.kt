package no.nav.helse.hops.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

object IdentityGenerator {
    /** Used to create deterministic UUID. **/
    fun CreateUUID5(namespace: String, name: String) = CreateUUID5(UUID.fromString(namespace), name)

    /** Used to create deterministic UUID. **/
    fun CreateUUID5(namespace: UUID, name: String) = Generators.nameBasedGenerator(namespace).generate(name)!!
}
