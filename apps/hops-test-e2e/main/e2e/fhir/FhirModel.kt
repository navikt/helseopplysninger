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
    val id: String = UUID.randomUUID().toString(),
    val resourceType: String,
    val type: String,
    @Serializable(LocalDateTimeSerde::class)
    val timestamp: LocalDateTime = LocalDateTime.now().toIso(),
    val entry: List<Entry>,
) : Resource()

@Serializable
data class Entry(
    val fullUrl: String,
    val resource: Resource,
)

@Serializable
data class MessageHeaderResource(
    val resourceType: String,
    val id: String,
    val eventCoding: EventCoding,
    val source: Source,
    val focus: List<Reference>,
) : Resource()

@Serializable
sealed class Resource

@Serializable
data class PatientResource(
    val resourceType: String,
    val id: String,
    val gender: String,
) : Resource()

@Serializable
data class EventCoding(
    val system: String,
    val code: String,
)

@Serializable
data class Source(val endpoint: String)

@Serializable
data class Reference(val reference: String)

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
