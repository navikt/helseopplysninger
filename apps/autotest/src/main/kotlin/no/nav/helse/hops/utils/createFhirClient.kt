package no.nav.helse.hops.utils

import ca.uhn.fhir.rest.client.api.IGenericClient
import no.nav.helse.hops.fhir.FhirClientFactory
import java.net.URL

fun createFhirClient(): IGenericClient {
    val env = DockerComposeEnv()

    val config = FhirClientFactory.Config(
        baseUrl = URL(env.fhirBaseUrl),
        discoveryUrl = URL(env.discoveryUrl),
        clientId = "autotest-test-client-id",
        clientSecret = "autotest-test-secret",
        scope = "hops-fhir-hapi-server"
    )
    return FhirClientFactory.createWithAuth(config)
}
