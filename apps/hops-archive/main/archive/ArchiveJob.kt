package archive

import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.fhir.JsonConverter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Resource

class ArchiveJob(
    messageStream: FhirMessageStream,
    private val logger: Logger,
    private val archive: Dokarkiv,
    private val converter: FhirJsonToPdfConverter,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            try {
                messageStream.poll().collect(::addToArchive)
                isRunning = true
            } catch (ex: Throwable) {
                isRunning = false
                if (ex is CancellationException) throw ex
                logger.error("Error while publishing document to archive.", ex)
                delay(5000)
            }
        }
    }

    @Volatile
    var isRunning = true
        private set

    override fun close() {
        runBlocking {
            job.cancelAndJoin()
        }
    }

    private suspend fun addToArchive(message: FhirMessage) {
        require(ContentTypes.fhirJsonR4.match(message.contentType)) {
            "${message.contentType} is not a known Content-Type."
        }

        val bundle = JsonConverter.parse<Bundle>(message.content)
        val header = bundle.entryResourceOfType<MessageHeader>()
        val questionnaire = bundle.entryResourceOfType<Questionnaire>()
        val questionnaireResponse = bundle.entryResourceOfType<QuestionnaireResponse>()

        val journalpost = InngaaendeJournalpost(
            datoMottatt = bundle.meta.lastUpdated,
            eksternReferanseId = header.id,
            tittel = questionnaire.title,
            legeHpr = questionnaireResponse.author.identifier.value,
            brukerFnr = questionnaireResponse.subject.identifier.value,
            brevkode = questionnaire.url, // her må vi sikkert heller ha en kode
            arkiv = converter.convertToPdf(message.content, ContentType.parse(message.contentType)),
            original = message.content
        )

        archive.add(journalpost, forsoekFerdigstill = true)
    }
}

private inline fun <reified R : Resource> Bundle.entryResourceOfType() = entry.mapNotNull { it.resource as? R }.single()
