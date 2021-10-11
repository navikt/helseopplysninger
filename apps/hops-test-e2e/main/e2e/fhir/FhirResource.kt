package e2e.fhir

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}

object FhirResource {
    private val cache = mutableListOf<FhirContent>()

    fun get(predicate: (FhirContent) -> Boolean) = cache
        .filter(predicate)
        .also {
            log.debug("returned cached resources with ids: ${it.map { r -> r.id }.toList()}")
        }

    /**
     * The ID from the resource of type MessageHeader is the kafka key used by `hops-event-replay-kafka`
     */
    val FhirContent.resourceId: UUID
        get() = entry
            .map { it.resource }
            .filterIsInstance<MessageHeaderResource>()
            .single()
            .id
            .let(UUID::fromString)

    @OptIn(ExperimentalSerializationApi::class)
    fun decode(content: String): FhirContent? = runCatching<FhirContent> { Json.decodeFromString(content) }.getOrNull()

    private val json = Json { prettyPrint = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun encode(content: FhirContent): String? = runCatching<String> { json.encodeToString(content) }.getOrNull()

    fun create(): FhirContent {
        cache.removeIf { it.timestamp.plusMinutes(5) < LocalDateTime.now() }

        val resourceId = UUID.randomUUID()

        val pat1Entry = Entry(
            fullUrl = "http://acme.com/ehr/fhir/Patient/pat1",
            resource = PatientResource(
                id = "pat1",
                resourceType = "Patient",
                gender = "male",
            ),
        )
        val pat12Entry = Entry(
            fullUrl = "http://acme.com/ehr/fhir/Patient/pat12",
            resource = PatientResource(
                id = "pat12",
                resourceType = "Patient",
                gender = "other",
            ),
        )
        val messageEntry = Entry(
            fullUrl = "urn:uuid:$resourceId",
            resource = MessageHeaderResource(
                id = resourceId.toString(),
                resourceType = "MessageHeader",
                focus = listOf(
                    Reference(pat1Entry.fullUrl),
                    Reference(pat12Entry.fullUrl),
                ),
                source = Source(endpoint = "http://example.org/clients/ehr-lite"),
                eventCoding = EventCoding(
                    system = "http://example.org/fhir/message-events",
                    code = "patient-link",
                ),
            ),
        )
        val content = FhirContent(
            resourceType = "Bundle",
            type = "message",
            entry = listOf(
                messageEntry,
                pat1Entry,
                pat12Entry
            ),
        )
        cache.add(content)
        log.debug("created and cached resource with id $resourceId")
        return content
    }
}
