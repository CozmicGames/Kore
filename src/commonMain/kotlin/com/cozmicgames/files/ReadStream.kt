package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.read16
import com.cozmicgames.utils.extensions.read32
import com.cozmicgames.utils.extensions.read8
import kotlin.math.min

interface ReadStream : Disposable {
    val availableBytes: Int

    fun skip(length: Int)
    fun readBytes(length: Int, dest: ByteArray, offset: Int = 0)
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readFloat(): Float
    fun readString(charset: Charset = Charsets.UTF8): String
    fun readString(length: Int, charset: Charset = Charsets.UTF8): String
}

fun ReadStream.isEmpty() = availableBytes <= 0

fun ReadStream.isNotEmpty() = !isEmpty()

fun ReadStream.readInt8() = readByte().toInt()

fun ReadStream.readInt16LE() = (readInt8()) or (readInt8() shl 8)

fun ReadStream.readInt24LE() = (readInt8()) or (readInt8() shl 8) or (readInt8() shl 16)

fun ReadStream.readInt32LE() = (readInt8()) or (readInt8() shl 8) or (readInt8() shl 16) or (readInt8() shl 24)

fun ReadStream.readInt64LE() = readInt32LE().toLong() or (readInt32LE().toLong() shl 32)

fun ReadStream.readFloat32LE() = Float.fromBits(readInt32LE())

fun ReadStream.readFloat64LE() = Double.fromBits(readInt64LE())

fun ReadStream.readInt16BE() = (readInt8() shl 8) or (readInt8())

fun ReadStream.readInt24BE() = (readInt8() shl 16) or (readInt8() shl 8) or (readInt8())

fun ReadStream.readInt32BE() = (readInt8() shl 24) or (readInt8() shl 16) or (readInt8() shl 8) or (readInt8())

fun ReadStream.readInt64BE() = (readInt32BE().toLong() shl 32) or (readInt32BE().toLong())

fun ReadStream.readFloat32BE() = Float.fromBits(readInt32BE())

fun ReadStream.readFloat64BE() = Double.fromBits(readInt64BE())

fun ReadStream.readInt16(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt16LE()
    Files.Endianness.BIG_ENDIAN -> readInt16BE()
}

fun ReadStream.readInt24(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt24LE()
    Files.Endianness.BIG_ENDIAN -> readInt24BE()
}

fun ReadStream.readInt32(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt32LE()
    Files.Endianness.BIG_ENDIAN -> readInt32BE()
}

fun ReadStream.readInt64(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt64LE()
    Files.Endianness.BIG_ENDIAN -> readInt64BE()
}

fun ReadStream.readFloat32(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readFloat32LE()
    Files.Endianness.BIG_ENDIAN -> readFloat32BE()
}

fun ReadStream.readFloat64(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readFloat64LE()
    Files.Endianness.BIG_ENDIAN -> readFloat64BE()
}

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

fun ReadStream.readAllBytes(): ByteArray {
    val bytes = ByteArray(availableBytes)
    readBytes(bytes.size, bytes)
    return bytes
}

fun ReadStream.readChar(charset: Charset = Charsets.UTF8) = buildString {
    val bytes = ByteArray(charset.bytesPerChar)
    readBytes(bytes.size, bytes)
    charset.decode(this, bytes)
}

fun ReadStream.readLine(charset: Charset = Charsets.UTF8) = buildString {
    if (availableBytes > charset.bytesPerChar) {
        var current = readChar(charset)
        while (availableBytes > charset.bytesPerChar && current != "\n" && current != "\r") {
            append(current)
            current = readChar()
        }
    }
}

fun ReadStream.forEachLine(charset: Charset = Charsets.UTF8, block: (String) -> Unit) {
    while (availableBytes > 0) {
        val line = readLine(charset)
        if (line.isNotBlank())
            block(line)
    }
}

fun ReadStream.readLines(charset: Charset = Charsets.UTF8): List<String> {
    val lines = arrayListOf<String>()
    forEachLine(charset) {
        lines += it
    }
    return lines
}
