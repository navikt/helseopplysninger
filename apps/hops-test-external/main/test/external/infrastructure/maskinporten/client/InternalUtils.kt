package test.external.infrastructure.maskinporten.client

import java.util.Date

internal fun String.suffix(s: String) = if (endsWith(s)) this else plus(s)
internal infix fun Date.addSeconds(seconds: Int): Date = Date(time + seconds * ONE_SECOND_IN_MILLISECONDS)
internal const val ONE_SECOND_IN_MILLISECONDS = 1000
