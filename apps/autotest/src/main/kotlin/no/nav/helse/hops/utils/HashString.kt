package no.nav.helse.hops.utils

import java.security.MessageDigest

object HashString {

    fun md5(str: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(str.toByteArray())
        return byteArrayToHex(bytes)
    }

    private fun byteArrayToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
