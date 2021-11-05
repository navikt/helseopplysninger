package no.nav.helse.hops.test.utils

import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

object SocketUtil {

    private val reservedPorts = ConcurrentHashMap<Int, Unit>()

    /**
     * To find a free port we have to use the port. That's why we have to free it again right before usage.
     * When running tests in parallell the same port can be found free before it is used.
     * This is resolved by reserving the issued port in a map between test runs.
     * Alternatively it can be solved by passing the socket to the server using it (not supported by e.g. ktor)
     */
    fun getAvailablePort(): Int {
        val port = ServerSocket(0).use(ServerSocket::getLocalPort)

        return when (reservedPorts.contains(port)) {
            true -> getAvailablePort()
            false -> port.also { reservedPorts[it] = Unit }
        }
    }
}
