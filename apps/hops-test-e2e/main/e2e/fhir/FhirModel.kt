package e2e.fhir

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@Serializable
data class FhirContent(
    override val resourceType: ResourceType = ResourceType.Bundle,
    override val id: String = UUID.randomUUID().toString(),
    val type: String = "message",
    @Serializable(LocalDateTimeSerde::class)
    val timestamp: LocalDateTime = LocalDateTime.now().toIso(),
    val entry: List<Entry>,
) : Resource()

@Serializable
data class Entry(
    val fullUrl: String = "urn:uuid:${UUID.randomUUID()}",
    val resource: Resource,
)

@Serializable
data class MessageHeaderResource(
    override val resourceType: ResourceType = ResourceType.MessageHeader,
    override val id: String,
    val eventCoding: EventCoding = EventCoding(),
    val source: Source = Source(),
    val focus: List<Reference> = listOf(Reference(), Reference("http://acme.com/ehr/fhir/Patient/pat12")),
) : Resource()

@Serializable
data class PatientResource(
    override val resourceType: ResourceType = ResourceType.Patient,
    override val id: String = "pat1",
    val gender: String = "male",
) : Resource()

@Serializable
sealed class Resource {
    abstract val resourceType: ResourceType
    abstract val id: String
}

@Serializable
enum class ResourceType { Bundle, MessageHeader, Patient }

@Serializable
data class EventCoding(
    val system: String = "http://example.org/fhir/message-events",
    val code: String = "patient-link",
)

@JvmInline
@Serializable
value class Source(val endpoint: String = "http://example.org/clients/ehr-lite")

@JvmInline
@Serializable
value class Reference(val reference: String = "http://acme.com/ehr/fhir/Patient/pat1")

@Serializer(forClass = LocalDateTime::class)
@OptIn(ExperimentalSerializationApi::class)
object LocalDateTimeSerde : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.toIsoString())
    override fun deserialize(decoder: Decoder): LocalDateTime = decoder.decodeString().toLocalDateTime()
}

private fun LocalDateTime.toIso() = toIsoString().toLocalDateTime()

/** YYYY-MM-DDThh:mm:ss.sssZ **/
private fun String.toLocalDateTime(): LocalDateTime =
    Instant.parse(this)
        .atZone(ZoneId.of("Europe/Oslo"))
        .truncatedTo(ChronoUnit.MILLIS)
        .toLocalDateTime()

/** YYYY-MM-DDThh:mm:ss.sssZ **/
private fun LocalDateTime.toIsoString(): String =
    atZone(ZoneId.of("Europe/Oslo"))
        .truncatedTo(ChronoUnit.MILLIS)
        .format(DateTimeFormatter.ISO_INSTANT)
