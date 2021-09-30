package e2e.fhir

import org.intellij.lang.annotations.Language
import java.util.UUID

object FhirResource {
    var id: UUID = UUID.randomUUID()
    var resource: String = generate(id)

    fun generate(): String {
        id = UUID.randomUUID()
        resource = generate(id)
        return resource
    }

    @Language("json")
    private fun generate(id: UUID): String = """
    {
      "resourceType": "Bundle",
      "id": "$id",
      "type": "message",
      "timestamp": "2015-07-14T11:15:33+10:00",
      "entry": [
        {
          "fullUrl": "urn:uuid:267b18ce-3d37-4581-9baa-6fada338038b",
          "resource": {
            "resourceType": "MessageHeader",
            "id": "267b18ce-3d37-4581-9baa-6fada338038b",
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
