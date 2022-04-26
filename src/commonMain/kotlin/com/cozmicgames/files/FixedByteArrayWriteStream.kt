package com.cozmicgames.files

import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.extensions.write16
import com.cozmicgames.utils.extensions.write32

/**
 * A stream for writing bytes to an existing byte array.
 *
 * @param bytes The array to write to.
 * @param offset The offset to start writing at. Defaults to 0.
 * @param maxLength The maximum number of bytes to write. Defaults to the length of the array.
 */
class FixedByteArrayWriteStream(private val bytes: ByteArray, private val offset: Int = 0, private val maxLength: Int = bytes.size - offset) : WriteStream {
    private var pointer = 0

    override val writtenBytes get() = pointer

    override fun writeBytes(values: ByteArray, offset: Int, length: Int) {
        require(maxLength - pointer >= length)

        values.copyInto(bytes, this.offset + pointer, offset, length)

        pointer += length
    }

    override fun writeByte(value: Byte) {
        require(maxLength - pointer >= 1)

        bytes[offset + pointer] = value
        pointer++
    }

    override fun writeShort(value: Short) {
        write16(pointer, value.toInt()) { p, v ->
            bytes[p] = v
        }

        pointer += 2
    }

    override fun writeInt(value: Int) {
        write32(pointer, value) { p, v ->
            bytes[p] = v
        }

        pointer += 4
    }

    override fun writeFloat(value: Float) {
        writeInt(value.toBits())
    }

    override fun writeString(value: String, charset: Charset) {
        val builder = ByteArrayBuilder()
        charset.encode(builder, value)
        writeBytes(builder.toByteArray())
    }

    /**
     * Resets the stream to the beginning.
     */
    fun reset() {
        pointer = 0
    }

    override fun dispose() {}
}
