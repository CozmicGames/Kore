package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.write16
import com.cozmicgames.utils.extensions.write32


interface WriteStream : Disposable {
    val writtenBytes: Int

    fun writeBytes(values: ByteArray, offset: Int = 0, length: Int = values.size - offset)
    fun writeByte(value: Byte)
    fun writeShort(value: Short)
    fun writeInt(value: Int)
    fun writeFloat(value: Float)
    fun writeString(value: String, charset: Charset = Charsets.UTF8)
}

inline fun WriteStream.writeInt8(v: Int) = writeByte((v and 0xFF).toByte())

inline fun WriteStream.writeInt16LE(v: Int) {
    writeInt8(v and 0xFF)
    writeInt8((v ushr 8) and 0xFF)
}

inline fun WriteStream.writeInt24LE(v: Int) {
    writeInt8(v and 0xFF)
    writeInt8((v ushr 8) and 0xFF)
    writeInt8((v ushr 16) and 0xFF)
}

inline fun WriteStream.writeInt32LE(v: Int) {
    writeInt8(v and 0xFF)
    writeInt8((v ushr 8) and 0xFF)
    writeInt8((v ushr 16) and 0xFF)
    writeInt8((v ushr 24) and 0xFF)
}

inline fun WriteStream.writeInt64LE(v: Long) {
    writeInt32LE(v.toInt())
    writeInt32LE((v ushr 32).toInt())
}

inline fun WriteStream.writeFloat32LE(v: Float) = writeInt32LE(v.toRawBits())

inline fun WriteStream.writeFloat64LE(v: Double) = writeInt64LE(v.toRawBits())

inline fun WriteStream.writeInt16BE(v: Int) {
    writeInt8((v ushr 8) and 0xFF)
    writeInt8(v and 0xFF)
}

inline fun WriteStream.writeInt24BE(v: Int) {
    writeInt8((v ushr 16) and 0xFF)
    writeInt8((v ushr 8) and 0xFF)
    writeInt8(v and 0xFF)
}

inline fun WriteStream.writeInt32BE(v: Int) {
    writeInt8((v ushr 24) and 0xFF)
    writeInt8((v ushr 16) and 0xFF)
    writeInt8((v ushr 8) and 0xFF)
    writeInt8(v and 0xFF)
}

inline fun WriteStream.writeInt64BE(v: Long) {
    writeInt32BE((v ushr 32).toInt())
    writeInt32BE(v.toInt())
}

inline fun WriteStream.writeFloat32BE(v: Float) = writeInt32BE(v.toRawBits())

inline fun WriteStream.writeFloat64BE(v: Double) = writeInt64BE(v.toRawBits())

inline fun WriteStream.writeInt16(v: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt16LE(v)
    Files.Endianness.BIG_ENDIAN -> writeInt16BE(v)
}

inline fun WriteStream.writeInt24(v: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt24LE(v)
    Files.Endianness.BIG_ENDIAN -> writeInt24BE(v)
}

inline fun WriteStream.writeInt32(v: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt32LE(v)
    Files.Endianness.BIG_ENDIAN -> writeInt32BE(v)
}

inline fun WriteStream.writeInt64(v: Long, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt64LE(v)
    Files.Endianness.BIG_ENDIAN -> writeInt64BE(v)
}

inline fun WriteStream.writeFloat32(v: Float, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeFloat32LE(v)
    Files.Endianness.BIG_ENDIAN -> writeFloat32BE(v)
}

inline fun WriteStream.writeFloat64(v: Double, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeFloat64LE(v)
    Files.Endianness.BIG_ENDIAN -> writeFloat64BE(v)
}

class ByteArrayWriteStream : WriteStream {
    private val builder = ByteArrayBuilder()

    override val writtenBytes get() = builder.size

    override fun writeBytes(values: ByteArray, offset: Int, length: Int) {
        repeat(length) {
            builder.append(values[it + offset])
        }
    }

    override fun writeByte(value: Byte) {
        builder.append(value)
    }

    override fun writeShort(value: Short) {
        builder.append(value)
    }

    override fun writeInt(value: Int) {
        builder.append(value)
    }

    override fun writeFloat(value: Float) {
        builder.append(value)
    }

    override fun writeString(value: String, charset: Charset) {
        charset.encode(builder, value)
    }

    fun toByteArray() = builder.toByteArray()

    override fun dispose() {

    }
}

class FixedByteArrayWriteStream(private val bytes: ByteArray, private val offset: Int = 0, private val maxLength: Int = bytes.size) : WriteStream {
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

    fun reset() {
        pointer = 0
    }

    override fun dispose() {

    }
}

fun WriteStream.writeLine(line: String, charset: Charset = Charsets.UTF8) = writeString("$line\n", charset)
