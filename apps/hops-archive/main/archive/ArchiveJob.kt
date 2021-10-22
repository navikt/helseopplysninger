package archive

import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.plugin.FhirMessage
import no.nav.helse.hops.plugin.FhirMessageStream
import no.nav.helse.hops.plugin.fromKafkaRecord
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext

class ArchiveJob(
    messageStream: FhirMessageStream,
    private val logger: Logger,
    private val archive: Dokarkiv,
    private val converter: FhirJsonToPdfConverter,
    context: CoroutineContext
) {
    init {
        CoroutineScope(context).launch {
            while (isActive) {
                try {
                    messageStream.poll(::fromKafkaRecord).collect(::addToArchive)
                    isRunning = true
                } catch (ex: Throwable) {
                    isRunning = false
                    if (ex is CancellationException) throw ex
                    logger.error("Error while publishing document to archive.", ex)
                    delay(5000)
                }
            }
        }
    }

    @Volatile
    var isRunning = true
        private set

    private suspend fun addToArchive(message: FhirMessage) {
        require(ContentTypes.fhirJsonR4.match(message.contentType)) {
            "${message.contentType} is not a known Content-Type."
        }

        val bundle = JsonConverter.parse<Bundle>(message.content)
        val header = bundle.entryResourceOfType<MessageHeader>()
        val questionnaire = bundle.entryResourceOfType<Questionnaire>()
        val questionnaireResponse = bundle.entryResourceOfType<QuestionnaireResponse>()

        val doc = GenericJournalpost(
            type = Journalpost.Type.INNGAAENDE,
            datoMottatt = bundle.meta.lastUpdated,
            eksternReferanseId = header.idElement.idPart,
            tittel = questionnaire.title,
            tema = "SYM", // Sykemelding. Bør utledes fra innholdet i meldingen
            legeHpr = questionnaireResponse.author.identifier.value,
            brukerFnr = questionnaireResponse.subject.identifier.value,
            brevkode = questionnaireResponse.questionnaire,
            arkiv = converter.convertToPdf(message.content, ContentType.parse(message.contentType)),
            original = message.content
        )

        archive.add(doc, header.id)
    }
}

private inline fun <reified R : Resource> Bundle.entryResourceOfType() = entry.mapNotNull { it.resource as? R }.single()
