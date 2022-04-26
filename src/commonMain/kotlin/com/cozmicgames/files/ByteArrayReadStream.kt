package com.cozmicgames.files

import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.extensions.read16
import com.cozmicgames.utils.extensions.read32
import com.cozmicgames.utils.extensions.read8
import kotlin.math.min

/**
 * A stream that reads from a byte array.
 * @param bytes The byte array to read from.
 * @param offset The offset to start reading from. Defaults to 0.
 * @param length The length of the stream. Defaults to the length of the byte array.
 */
class ByteArrayReadStream(private val bytes: ByteArray, private val offset: Int = 0, private val length: Int = bytes.size) : ReadStream {
    private var position = 0

    override val availableBytes get() = length - position

    override fun skip(length: Int) {
        position += min(length, availableBytes)
    }

    override fun readBytes(length: Int, dest: ByteArray, offset: Int) {
        bytes.copyInto(dest, offset, position + offset, position + offset + length)
        position += length
    }

    override fun readByte(): Byte {
        val result = read8(position + offset) { bytes[it] }
        position += 1
        return result.toByte()
    }

    override fun readShort(): Short {
        val result = read16(position + offset) { bytes[it] }
        position += 2
        return result.toShort()
    }

    override fun readInt(): Int {
        val result = read32(position + offset) { bytes[it] }
        position += 4
        return result
    }

    override fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    override fun readString(charset: Charset): String {
        val builder = ByteArrayBuilder()

        while (true) {
            if (position + offset >= length)
                break

            val byte = readByte()
            if (byte == 0.toByte())
                break
        }

        return buildString {
            charset.decode(this, builder.toByteArray())
        }
    }

    override fun readString(length: Int, charset: Charset): String {
        val bytes = ByteArray(length)
        readBytes(length, bytes)
        return buildString {
            charset.decode(this, bytes)
        }
    }

    fun reset(): ByteArrayReadStream {
        position = 0
        return this
    }

    override fun dispose() {}
}
