package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.DataFormatException
import org.apache.kafka.common.serialization.Deserializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayInputStream

class KafkaFhirResourceDeserializer : Deserializer<IBaseResource> {
    override fun deserialize(topic: String?, data: ByteArray?): IBaseResource? {
        ByteArrayInputStream(data!!).use {
            val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)!!
            val jsonParser = fhirContext.newJsonParser()!!

            return try {
                jsonParser.parseResource(it)
            } catch (_: DataFormatException) {
                null // Unparsable FHIR Json will be silently ignored.
            }
        }
    }
}
