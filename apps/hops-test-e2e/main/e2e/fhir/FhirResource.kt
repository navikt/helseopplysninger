package e2e.fhir

import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private val log = KotlinLogging.logger {}

data class FhirContent(val id: UUID, val json: String, val timestamp: LocalDateTime = LocalDateTime.now())

object FhirResource {
    private val cache = mutableListOf<FhirContent>()

    fun get(predicate: (FhirContent) -> Boolean) = cache
        .filter(predicate)
        .also {
            log.debug("returned cached resources with ids: ${it.map { r -> r.id }.toList()}")
        }

    fun create(): FhirContent {
        cache.removeIf { it.timestamp.plusMinutes(5) < LocalDateTime.now() }
        val resourceId = UUID.randomUUID()
        val content = FhirContent(resourceId, json(resourceId))
        cache.add(content)
        return content
    }
}

/** YYYY-MM-DDThh:mm:ss.sssZ **/
private fun LocalDateTime.toIsoString(): String =
    atZone(ZoneId.of("Europe/Oslo"))
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

@Language("json")
private fun json(resourceId: UUID): String =
    """
    {
      "resourceType": "Bundle",
      "id": "${UUID.randomUUID()}",
      "type": "message",
      "timestamp": "${LocalDateTime.now().toIsoString()}",
      "entry": [
        {
          "fullUrl": "urn:uuid:$resourceId",
          "resource": {
            "resourceType": "MessageHeader",
            "id": "$resourceId",
            "eventCoding": {
              "system": "http://example.org/fhir/message-events",
              "code": "patient-link"
            },
            "source": {
              "endpoint": "http://example.org/clients/ehr-lite"
            },
            "focus": [
              {
                "reference": "http://acme.com/ehr/fhir/Patient/pat1"
              },
              {
                "reference": "http://acme.com/ehr/fhir/Patient/pat12"
              }
            ]
          }
        },
        {
          "fullUrl": "http://acme.com/ehr/fhir/Patient/pat1",
          "resource": {
            "resourceType": "Patient",
            "id": "pat1",
            "gender": "male"
          }
        },
        {
          "fullUrl": "http://acme.com/ehr/fhir/Patient/pat12",
          "resource": {
            "resourceType": "Patient",
            "id": "pat2",
            "gender": "other"
          }
        }
      ]
    }
    """.trimIndent()
