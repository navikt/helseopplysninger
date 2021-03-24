package no.nav.helse.hops.koinBootstrapping

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.apache.kafka.common.serialization.Serializer
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

class FhirResourceSerializer : Serializer<IBaseResource> {
    override fun serialize(topic: String?, data: IBaseResource?): ByteArray {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)!!
        val parser = ctx.newJsonParser()!!

        ByteArrayOutputStream().use { stream ->
            OutputStreamWriter(stream).use { writer ->
                parser.encodeResourceToWriter(data!!, writer)
            }

            return stream.toByteArray()
        }
    }
}
