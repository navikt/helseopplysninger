package e2e._common

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object TestUtils {

    fun retry(times: Int = 100, delayMs: Long = 100L, predicate: () -> Boolean): Boolean {
        var timesLeft = times
        var success = false

        fun remains() = timesLeft-- > 0
        fun wait() = runBlocking { delay(delayMs) }
        fun success() = predicate().also { success = it }

        while (!success() && remains()) wait()
        return success
    }
}
