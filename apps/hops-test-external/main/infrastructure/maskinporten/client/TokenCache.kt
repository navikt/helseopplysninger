package infrastructure.maskinporten.client

import com.nimbusds.jwt.SignedJWT
import java.util.Date

internal class TokenCache(token: String? = null) {
    internal val token = token?.let(SignedJWT::parse)
        get() = field?.takeUnless { it.isExpired }

    private val SignedJWT.isExpired: Boolean
        get() = jwtClaimsSet?.expirationTime?.is20SecondsPrior?.not() ?: false

    private val Date.is20SecondsPrior: Boolean
        get() = epochSeconds - (now.epochSeconds + TWENTY_SECONDS) >= 0

    private val Date.epochSeconds: Long
        get() = time / 1000

    private val now: Date
        get() = Date()

    companion object {
        private const val TWENTY_SECONDS = 20
    }
}
