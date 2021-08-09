package no.nav.helse.hops.hops.fhir

import com.google.auth.oauth2.GoogleCredentials
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL

/**
 * @todo Skrive bedre tester på dette når jeg finner ut hvordan man mocker i Kotlin
 * Beholder dem her for manuell testing.
 */
internal class FhirClientGcpFactoryTest {

    @Test
    @Disabled
    fun `getting google default credential`() {
        // https://github.com/googleapis/google-auth-library-java
        val credentials = GoogleCredentials
            .getApplicationDefault()
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        credentials.refreshIfExpired()

        println(credentials)
        println(credentials.accessToken)
    }

    @Test
    @Disabled
    fun `getting fhirclient`() {
        val config = FhirClientGcpFactory
            .Config(baseUrl = URL("https://healthcare.googleapis.com/v1/projects/helseopplysninger-dev-d4b0/locations/europe-west4/datasets/hops/fhirStores/exploding-rabbit/fhir"))
        val fhirClient = FhirClientGcpFactory.createWithAuth(config)
        val result = fhirClient
            .search<Bundle>()
            .byUrl("Patient")
            .execute()
        println(result.toJson())
    }
}
