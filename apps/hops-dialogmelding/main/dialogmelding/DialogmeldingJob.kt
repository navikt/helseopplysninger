package dialogmelding

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
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.domain.HerId
import no.nav.helse.hops.domain.HprNr
import no.nav.helse.hops.domain.PersonId
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.plugin.FhirMessage
import no.nav.helse.hops.plugin.MessageStream
import no.nav.helse.hops.plugin.fromKafkaRecord
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.map

class DialogmeldingJob(
    messageStream: MessageStream,
    private val logger: Logger,
    private val converter: FhirJsonToPdfConverter,
    private val mq: MQSender,
    context: CoroutineContext = Dispatchers.Default
) : AutoCloseable {
    private val job = CoroutineScope(context).launch {
        while (isActive) {
            try {
                messageStream.poll(::fromKafkaRecord).map(::createDialogmelding).collect { mq.send(it) }
                isRunning = true
            } catch (ex: Throwable) {
                isRunning = false
                if (ex is CancellationException) throw ex
                logger.error("Error while publishing message to IBM MQ.", ex)
                delay(5000)
            }
        }
    }

    @Volatile
    var isRunning = true
        private set

    override fun close() {
        if (!job.isCompleted) {
            runBlocking {
                job.cancelAndJoin()
            }
        }
    }

    private suspend fun createDialogmelding(message: FhirMessage): String {
        require(ContentTypes.fhirJsonR4.match(message.contentType)) {
            "${message.contentType} is not a known Content-Type."
        }

        val bundle = JsonConverter.parse<Bundle>(message.content)
        val header = bundle.entryResourceOfType<MessageHeader>()
        val questionnaireResponse = bundle.entryResourceOfType<QuestionnaireResponse>()

        val destName = header.source.name!! // Bør nok hentes fra adresseregisteret eller annet element
        val herId = HerId(header.source.endpoint.split('.').last())
        val patientId = PersonId(questionnaireResponse.subject.identifier.value)
        val practitionerId = HprNr(questionnaireResponse.author.identifier.value)
        val pdf = converter.convertToPdf(message.content, ContentType.parse(message.contentType))

        val meta = DialogNotatMeta(
            header.idAsUUID(),
            bundle.timestamp,
            DialogNotatMeta.Mottaker(herId, destName),
            DialogNotatMeta.Pasient(patientId),
            DialogNotatMeta.Behandler(practitionerId),
            pdf
        )

        return DialogmeldingFactory.createNoteFromNavToPractitioner(meta)
    }
}

private inline fun <reified R : Resource> Bundle.entryResourceOfType() = entry.mapNotNull { it.resource as? R }.single()
