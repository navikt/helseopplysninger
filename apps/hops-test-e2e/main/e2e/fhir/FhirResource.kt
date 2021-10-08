package e2e.fhir

import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}

object FhirResource {
    data class Resource(val id: UUID, val content: String, val timestamp: LocalDateTime)

    private val cache = mutableListOf<Resource>()

    fun create(): Resource {
        cache.removeIf { it.timestamp.plusMinutes(5) < LocalDateTime.now() }
        val id = UUID.randomUUID()
        val resource = Resource(id, content(id, UUID.randomUUID()), LocalDateTime.now())
        cache.add(resource)
        log.info("created resource for id $id")
        return resource
    }

    fun get(predicate: (Resource) -> Boolean) = cache
        .filter(predicate)
        .also { log.info("retrieved resources with ids: ${it.map { r -> r.id }.toList()}") }

    @Language("json")
    private fun content(id: UUID, resourceId: UUID): String = """
    {
      "resourceType": "Bundle",
      "id": "$id",
      "type": "message",
      "timestamp": "2015-07-14T11:15:33+10:00",
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
}
