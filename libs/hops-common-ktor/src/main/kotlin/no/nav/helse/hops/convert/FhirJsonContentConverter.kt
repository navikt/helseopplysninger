package no.nav.helse.hops.convert

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import io.ktor.application.ApplicationCall
import io.ktor.content.TextContent
import io.ktor.features.ContentConverter
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.use
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hl7.fhir.instance.model.api.IBaseResource

class FhirJsonContentConverter : ContentConverter {
    private fun newParser() = FhirContext
        .forCached(FhirVersionEnum.R4)
        .newJsonParser()
        .setPrettyPrint(true)
        .setOverrideResourceIdWithBundleEntryFullUrl(false)

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        val resource = value as? IBaseResource ?: return null
        val json = newParser().encodeResourceToString(resource)
        return TextContent(json, contentType.withCharset(Charsets.UTF_8))
    }

    override suspend fun convertForReceive(
        context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>
    ): Any? {
        val channel = context.subject.value as? ByteReadChannel ?: return null
        return withContext(Dispatchers.IO) {
            channel.toInputStream().reader().use {
                newParser().parseResource(it)
            }
        }
    }
}
