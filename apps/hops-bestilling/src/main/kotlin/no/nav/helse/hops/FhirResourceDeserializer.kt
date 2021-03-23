package no.nav.helse.hops

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.apache.kafka.common.serialization.Deserializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayInputStream

class FhirResourceDeserializer : Deserializer<IBaseResource> {
    override fun deserialize(topic: String?, data: ByteArray?): IBaseResource {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)!!
        val parser = ctx.newJsonParser()!!

        ByteArrayInputStream(data!!).use {
            return parser.parseResource(it)
        }
    }
}
