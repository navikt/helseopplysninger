package e2e.fhir

import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.util.UUID

private val log = KotlinLogging.logger {}

object FhirResource {
    val cache = mutableMapOf<UUID, String>()

    fun generate(): Pair<UUID, String> {
        val id = UUID.randomUUID()
        val resourceId = UUID.randomUUID()
        val resource = resource(id, resourceId)
        cache[id] = resource
        log.info("created resource for id $id")
        return id to resource
    }

    fun getAndRemove(id: UUID) = cache.remove(id).also {
        log.info("removed resoruce for id $id")
    }

    @Language("json")
    private fun resource(id: UUID, resourceId: UUID): String = """
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
