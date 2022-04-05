package com.cozmicgames.utils

import com.cozmicgames.utils.extensions.format
import kotlin.random.Random

class UUID(private val data: ByteArray) {
    companion object {
        private val regex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", RegexOption.IGNORE_CASE)

        private fun fix(data: ByteArray, version: Int, variant: Int): ByteArray {
            data[6] = ((data[6].toInt() and 0b00001111) or (version shl 4)).toByte()
            data[8] = ((data[8].toInt() and 0x00111111) or (variant shl 6)).toByte()
            return data
        }

        fun random(random: Random = Random): UUID = UUID(fix(random.nextBytes(16), 4, 1))

        operator fun invoke(str: String): UUID {
            require(regex.matchEntire(str) != null) { "Invalid UUID" }
            return UUID(Hex.decode(str.replace("-", "")))
        }
    }

    val version: Int get() = (data[6].toInt() ushr 4) and 0b1111

    val variant: Int get() = (data[8].toInt() ushr 6) and 0b11

    private val hashCode = data.contentHashCode()

    override fun toString() = "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x".format(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15])

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UUID) return false

        if (hashCode != other.hashCode)
            return false

        if (!data.contentEquals(other.data))
            return false

        return true
    }
}