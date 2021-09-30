package e2e._common

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object TestUtils {

    fun retry(
        times: Int = 100,
        delayMs: Long = 100L,
        predicate: () -> Boolean
    ): Boolean {
        var remainder = times
        var success = false

        fun remains() = remainder-- > 0

        fun wait(): Boolean = runBlocking {
            delay(delayMs)
            true
        }

        fun unsuccessful(): Boolean {
            success = predicate()
            return success.not()
        }

        while (unsuccessful() && remains()) {
            wait()
        }

        return success
    }
}
