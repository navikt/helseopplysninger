package e2e

import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

private val log = KotlinLogging.logger {}

fun <R> withTestApp(test: TestApplicationEngine.() -> R): R =
    EnvironmentVariables(config).execute<R> {
        withTestApplication(Application::main, test)
    }

private val config: Map<String, String>
    get() = mapOf(
        "API_HOST" to Mocks.api.getBaseUrl(),
        "API_HOST_EXTERNAL" to Mocks.api.getBaseUrl(),
        "EVENT_REPLAY_KAFKA_HOST" to Mocks.eventreplay.getBaseUrl(),
        "EVENT_SINK_KAFKA_HOST" to Mocks.eventsink.getBaseUrl(),
        "EVENT_STORE_HOST" to Mocks.eventstore.getBaseUrl(),
        "MASKINPORTEN_WELL_KNOWN_URL" to Mocks.maskinporten.getBaseUrl(),
        "MASKINPORTEN_CLIENT_ID" to "e2e-test-id",
        "MASKINPORTEN_CLIENT_JWK" to jwk,
        "MASKINPORTEN_SCOPES" to "nav:helse:helseopplysninger.read nav:helse:helseopplysninger.write",
        "KAFKA_BROKERS" to EmbeddedKafka.getHost(),
        "KAFKA_CLIENT_ID" to "hops-test-e2e-$kafkaPrefix",
        "KAFKA_GROUP_ID" to "hops-test-e2e-$kafkaPrefix"
    )

private val kafkaPrefix: Int get() = (0..100).random()

@Language("json")
private val jwk =
    """
{
  "p":"zsPY7ILYO-SD_AsuMPm56EJuVcnytlcE_XVmIWQufOPzThlMsyKKqCioBxWdzsNgHw0tRgN7Zh6YOP4syi2HhvVDD0lnhB5JGX3q8AzlVtyWpjrGMXF3lLPzDQ8D4pc5itGZHpQX-CYu2Wo7W0xmZaTR-U-ya_-UwxzL43RbQGk",
  "kty":"RSA",
  "x5t#S256":"qBGrisvpRXxyL89gbRzXW142L3Kt5TgZmRPJ5osF8q4",
  "q":"vESCuWLiFVp-dOVbk5oddU2_MHJxawN3HFPhIK-7wYi6LXreuhz1JfGwWzEogLIeH1E-oIeh_cxca_K3L_WXwYexeEtS_DxgCsvNHB2aWN2_7-Iq8ZNxUi918xJq2CI3m9RvLz6O5Zy0eF6qt9Zz8Ga64zlsToERsGWFfN1jnPs",
  "d":"J_mMSpq8k4WH9GKeS6d1kPVrQz2jDslAy3b3zrBuiSdNtKgUN7jFhGXaiY-cAg3efhMc-MWwPa0raKEN9xQRtIdbJurJbNG3viCvo_8FNs5lmFCUIktuO12zvsJS63q-i1zsZ7_esYQHbeDqg9S3q98c2EIO8lxQvPBcq-OIjdxfuanAEWJIRNuvNkK5I0AcqF_Q_KeFQDHo5sWUkwyPCaddd-ogS_YDeK3eeUpQbElrusdv0Ai0iYBPukzEHz1aL8PbaYru9f6Alor6yt9Lc_FNKfi-gnNFdpg3-uqVEh-MhEXgyN1RkeZzt0Kk9rylHumjSpwEgzuuA2L3WnycUQ",
  "e":"AQAB",
  "kid":"jlAX4HYKW4hyhZgSmUyOmVAqMUw",
  "x5c":[
    "MIIDfTCCAmWgAwIBAgIEAVDRZjANBgkqhkiG9w0BAQsFADBuMQswCQYDVQQGEwJubzEPMA0GA1UECBMGb2F1dGgyMQ8wDQYDVQQHEwZvYXV0aDIxDzANBgNVBAoTBm9hdXRoMjEPMA0GA1UECxMGb2F1dGgyMRswGQYDVQQDExJvYXV0aDIgdGVzdCBjbGllbnQwIBcNMTkxMDI1MTI1OTIwWhgPMjExOTEwMDExMjU5MjBaMG4xCzAJBgNVBAYTAm5vMQ8wDQYDVQQIEwZvYXV0aDIxDzANBgNVBAcTBm9hdXRoMjEPMA0GA1UEChMGb2F1dGgyMQ8wDQYDVQQLEwZvYXV0aDIxGzAZBgNVBAMTEm9hdXRoMiB0ZXN0IGNsaWVudDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJgPKOh+dv33SPg8yDWyEc6QozeouYB8znyZ7MwpYn3wj0vhSQcDXJJwDFqNCDY7ePatgV9q1YJ3F+8v1sEakGfJ6OJL7tFJVfwdB8f+Hbb6jYZHxidACKjaWYuRiS\/qCgvKNUsOIx3kOnPffr2da5IWVA+dcgvn2ytMtaLW1U1UWCRrET0HFkJhuxkwxTdOISPlF\/3X+17XOvnMj3TLprDii3tR5iO0CiwEa3nx21y8fKEvF8Mnj+cLqe1+x+4KCqXoBXCY7aN5nhXL2+69DrzcTRY1qEJ+5h0aBTbaLBd2RZi1x6Fhy3VqSjepHOs3WWlVplHst0\/Ia\/oIqz5TIvMCAwEAAaMhMB8wHQYDVR0OBBYEFDSUH4EHAl7n2QlGBZ4N3q6fPoY4MA0GCSqGSIb3DQEBCwUAA4IBAQAKQIpcalpK\/dOxU2ImkA5+lX4IZb\/TCtk8HDB\/bR4+AI02P\/UDV4gFaesrIylBFnpTtloVQQ9deH261aeBowl6rqSzzR1KN8EUwEw67DjLmVkOG6Sdq8BkvtWE0w0O+aMJn5QRi2CNQRNpi57iG+KMOlQx4aH9E6qoHXsnLeTmdh882pl2DBLHIbyx8hl\/SHfzhhSI1r3BNIpsZKDlLC9P90x9CzhC0cMF+b7YFmtSit\/776YAdasyHbYvu66VaQZdsiY3z7JyUhJmaaGCR6VDLSEK2Y4JayMQiqUlSKiRlspZMR+dHmfCnUn3ZtGDLSJlOBCDDEz3EviYPj4dfCXn"
  ],
  "qi":"iivf7LsAksBnetH-enol8_PJC8gXapdET4pD0mLHQ5Pjuux9Yz18ds0ECvVADD3QmxsknNogaPSSldH9gAB6g5fqURi12QLarFlrWjHsVEtcI3s7XWfVtwLGFm-bW0KJnOJ9PW8wfSc7tc3e6bDKkYN_ekDvRhRdon9F3bnyYy0",
  "dp":"bGdR--494GjWqfZSqWrEhXkOz_upPOAyxZAfk7IqjWAV2AR7qg-aEr_-GHjE2_qjEqSd7-8zaz7vIDJi2T01qRQ9rG4Xz7TxLmROIL0iIIBWm6CE-Lc8ssIF0_rjVpFiod1yIg4S4w9h0KtZo2xS40eers-SA_1jyUf3vbDrhsE",
  "dq":"dzYCeJTuh4rnq-lXVV0u7gou1-R_gL2O_Hb4hJQCFYgYK5gz1DFl4YLqorO769HdVQNC3q9Dmct_cjMcX9fpIfhkHcHEaEdqoStvUzBDfaXcVW8mthUgmmPHEgVFdlokUB3x0T6RiT7y341CGGpIu56xFBRWSldb9hAyuGAPJWU",
  "n":"mA8o6H52_fdI-DzINbIRzpCjN6i5gHzOfJnszCliffCPS-FJBwNcknAMWo0INjt49q2BX2rVgncX7y_WwRqQZ8no4kvu0UlV_B0Hx_4dtvqNhkfGJ0AIqNpZi5GJL-oKC8o1Sw4jHeQ6c99-vZ1rkhZUD51yC-fbK0y1otbVTVRYJGsRPQcWQmG7GTDFN04hI-UX_df7Xtc6-cyPdMumsOKLe1HmI7QKLARrefHbXLx8oS8XwyeP5wup7X7H7goKpegFcJjto3meFcvb7r0OvNxNFjWoQn7mHRoFNtosF3ZFmLXHoWHLdWpKN6kc6zdZaVWmUey3T8hr-girPlMi8w"
} 
    """.trimIndent()
