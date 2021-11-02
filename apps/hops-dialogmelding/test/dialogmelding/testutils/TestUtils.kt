package dialogmelding.testutils

import java.net.ServerSocket

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}

fun readResourcesFile(path: String) =
    object {}.javaClass.getResource(path)!!.readBytes()

fun readResourcesFileAsString(path: String) =
    object {}.javaClass.getResource(path)!!.readText()
