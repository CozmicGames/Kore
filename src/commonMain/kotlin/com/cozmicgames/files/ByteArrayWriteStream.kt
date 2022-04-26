package com.cozmicgames.files

import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset

/**
 * A stream for writing bytes to a byte array.
 * The resulting byte array can be accessed via [ByteArrayWriteStream.toByteArray].
 */
class ByteArrayWriteStream : WriteStream {
    private val builder = ByteArrayBuilder()

    /**
     * @see WriteStream.writtenBytes
     */
    override val writtenBytes get() = builder.size

    /**
     * @see WriteStream.writeBytes
     */
    override fun writeBytes(values: ByteArray, offset: Int, length: Int) {
        repeat(length) {
            builder.append(values[it + offset])
        }
    }

    /**
     * @see WriteStream.writeByte
     */
    override fun writeByte(value: Byte) {
        builder.append(value)
    }

    /**
     * @see WriteStream.writeShort
     */
    override fun writeShort(value: Short) {
        builder.append(value)
    }

    /**
     * @see WriteStream.writeInt
     */
    override fun writeInt(value: Int) {
        builder.append(value)
    }

    /**
     * @see WriteStream.writeFloat
     */
    override fun writeFloat(value: Float) {
        builder.append(value)
    }

    /**
     * @see WriteStream.writeString
     */
    override fun writeString(value: String, charset: Charset) {
        charset.encode(builder, value)
    }

    /**
     * Returns the byte array.
     *
     * @return the byte array
     */
    fun toByteArray() = builder.toByteArray()

    override fun dispose() {}
}
