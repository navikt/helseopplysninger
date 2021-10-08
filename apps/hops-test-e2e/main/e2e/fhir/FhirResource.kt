package e2e.fhir

import org.intellij.lang.annotations.Language
import java.util.UUID

object FhirResource {
    private var resourceId: UUID = UUID.randomUUID()
    var id: UUID = UUID.randomUUID()
    var resource: String = resource(id, resourceId)

    fun generate(): String {
        id = UUID.randomUUID()
        resourceId = UUID.randomUUID()
        resource = resource(id, resourceId)
        return resource
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
