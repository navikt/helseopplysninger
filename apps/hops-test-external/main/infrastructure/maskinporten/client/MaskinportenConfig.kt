package infrastructure.maskinporten.client

import com.nimbusds.jose.jwk.RSAKey
import java.net.ProxySelector

data class MaskinportenConfig(
    internal val baseUrl: String,
    internal val clientId: String,
    internal val privateKey: RSAKey,
    internal val scope: String,
    internal val validInSeconds: Int = 120,
    internal val proxy: ProxySelector = ProxySelector.getDefault(),
    internal val jti: String? = null,
    internal val resource: String? = null
) {
    internal val issuer = baseUrl.suffix("/")
}
