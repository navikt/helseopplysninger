package no.nav.helse.hops.utils

import ca.uhn.fhir.context.FhirContext
import io.github.cdimascio.dotenv.dotenv
import org.hl7.fhir.r4.model.Bundle
import java.io.File

class Fixtures {
    fun bestillingsBundle(): Bundle {
        val ctx = FhirContext.forR4()
        val parser = ctx.newJsonParser().setPrettyPrint(true)
        val content = this::class.java.getResource("/fixtures/bestilling.json").readText()
        return parser.parseResource(content) as Bundle
    }
}
