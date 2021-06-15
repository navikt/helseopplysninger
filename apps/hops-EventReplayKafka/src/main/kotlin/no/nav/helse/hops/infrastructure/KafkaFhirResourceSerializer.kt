package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.apache.kafka.common.serialization.Serializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

class KafkaFhirResourceSerializer : Serializer<IBaseResource> {
    override fun serialize(topic: String?, data: IBaseResource?): ByteArray {
        ByteArrayOutputStream().use { stream ->
            OutputStreamWriter(stream).use { writer ->
                val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)
                val jsonParser = fhirContext.newJsonParser()
                jsonParser.encodeResourceToWriter(data!!, writer)
            }

            return stream.toByteArray()
        }
    }
}
