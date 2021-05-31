package no.nav.helse.hops.convert

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.content.TextContent
import io.ktor.features.ContentConverter
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import org.hl7.fhir.instance.model.api.IBaseResource

class FhirJsonContentConverter : ContentConverter {
    private val fhirCtx = FhirContext.forCached(FhirVersionEnum.R4)!!

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any {
        require(value is IBaseResource)

        val json = fhirCtx.newJsonParser().encodeResourceToString(value)
        return TextContent(json, contentType.withCharset(context.call.suitableCharset()))
    }

    override suspend fun convertForReceive(
        context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null

        val charset = context.call.request.contentCharset() ?: Charsets.UTF_8
        val reader = channel.toInputStream().reader(charset)

        return fhirCtx.newJsonParser().parseResource(reader)
    }
}
