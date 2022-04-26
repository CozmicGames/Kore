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
    /**
     * The available number of bytes in the stream.
     */
    val availableBytes: Int

    /**
     * Skips the specified number of bytes in the stream.
     *
     * @param length The number of bytes to skip.
     */
    fun skip(length: Int)

    /**
     * Reads the specified number of bytes from the stream into the specified [ByteArray] at the [offset].
     *
     * @param length The number of bytes to read.
     * @param dest The destination [ByteArray].
     * @param offset The offset into the [ByteArray] to read the bytes into.
     */
    fun readBytes(length: Int, dest: ByteArray, offset: Int = 0)

    /**
     * Reads a single byte from the stream.
     *
     * @return The byte read from the stream.
     */
    fun readByte(): Byte

    /**
     * Reads a single short from the stream.
     *
     * @return The short read from the stream.
     */
    fun readShort(): Short

    /**
     * Reads a single int from the stream.
     *
     * @return The int read from the stream.
     */
    fun readInt(): Int

    /**
     * Reads a single float from the stream.
     *
     * @return The float read from the stream.
     */
    fun readFloat(): Float

    /**
     * Reads the whole stream as a string with the specified [charset].
     *
     * @param charset The charset to use when reading the stream.
     *
     * @return The string read from the stream.
     */
    fun readString(charset: Charset = Charsets.UTF8): String

    /**
     * Reads a string with the specified [charset] and the specified [length] from the stream.
     *
     * @param charset The charset to use when reading the stream.
     *
     * @return The string read from the stream.
     */
    fun readString(length: Int, charset: Charset = Charsets.UTF8): String
}

/**
 * Returns true if the stream has no available bytes left.
 *
 * @return the value read from the stream.
 */
fun ReadStream.isEmpty() = availableBytes <= 0

/**
 * Returns true if the stream has available bytes left.
 *
 * @return the value read from the stream.
 */
fun ReadStream.isNotEmpty() = !isEmpty()

/**
 * Reads an 8-bit integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt8() = readByte().toInt()

/**
 * Reads a 16-bit little endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt16LE() = (readInt8()) or (readInt8() shl 8)

/**
 * Reads a 24-bit little endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt24LE() = (readInt8()) or (readInt8() shl 8) or (readInt8() shl 16)

/**
 * Reads a 32-bit little endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt32LE() = (readInt8()) or (readInt8() shl 8) or (readInt8() shl 16) or (readInt8() shl 24)

/**
 * Reads a 64-bit little endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt64LE() = readInt32LE().toLong() or (readInt32LE().toLong() shl 32)

/**
 * Reads a 32-bit little endian float value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat32LE() = Float.fromBits(readInt32LE())

/**
 * Reads a 64-bit little endian float value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat64LE() = Double.fromBits(readInt64LE())

/**
 * Reads a 16-bit big endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt16BE() = (readInt8() shl 8) or (readInt8())

/**
 * Reads a 24-bit big endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt24BE() = (readInt8() shl 16) or (readInt8() shl 8) or (readInt8())

/**
 * Reads a 32-bit big endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt32BE() = (readInt8() shl 24) or (readInt8() shl 16) or (readInt8() shl 8) or (readInt8())

/**
 * Reads a 64-bit big endian integer value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt64BE() = (readInt32BE().toLong() shl 32) or (readInt32BE().toLong())

/**
 * Reads a 32-bit big endian float value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat32BE() = Float.fromBits(readInt32BE())

/**
 * Reads a 64-bit big endian float value from the stream.
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat64BE() = Double.fromBits(readInt64BE())

/**
 * Reads a 16-bit integer value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt16(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt16LE()
    Files.Endianness.BIG_ENDIAN -> readInt16BE()
}

/**
 * Reads a 24-bit integer value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt24(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt24LE()
    Files.Endianness.BIG_ENDIAN -> readInt24BE()
}

/**
 * Reads a 32-bit integer value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt32(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt32LE()
    Files.Endianness.BIG_ENDIAN -> readInt32BE()
}

/**
 * Reads a 64-bit integer value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readInt64(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readInt64LE()
    Files.Endianness.BIG_ENDIAN -> readInt64BE()
}

/**
 * Reads a 32-bit float value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat32(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readFloat32LE()
    Files.Endianness.BIG_ENDIAN -> readFloat32BE()
}

/**
 * Reads a 64-bit float value from the stream with the specified [endianness].
 *
 * @return the value read from the stream.
 */
fun ReadStream.readFloat64(endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> readFloat64LE()
    Files.Endianness.BIG_ENDIAN -> readFloat64BE()
}

/**
 * Reads all bytes from the stream.
 *
 * @return The bytes read from the stream.
 */
fun ReadStream.readAllBytes(): ByteArray {
    val bytes = ByteArray(availableBytes)
    readBytes(bytes.size, bytes)
    return bytes
}

/**
 * Reads a single character from the stream with the specified [charset].
 *
 * @param charset The charset to use.
 *
 * @return the character read from the stream.
 */
fun ReadStream.readChar(charset: Charset = Charsets.UTF8) = buildString {
    val bytes = ByteArray(charset.bytesPerChar)
    readBytes(bytes.size, bytes)
    charset.decode(this, bytes)
}

/**
 * Reads a line of a string from the stream with the specified [charset].
 * A line is separated by a carriage return or a line feed.
 * The returned string does not contain the line separator.
 * If the stream is empty, returns an empty string.
 *
 * @param charset The charset to use.
 *
 * @return the line read from the stream.
 */
fun ReadStream.readLine(charset: Charset = Charsets.UTF8) = buildString {
    if (availableBytes > charset.bytesPerChar) {
        var current = readChar(charset)
        while (availableBytes > charset.bytesPerChar && current != "\n" && current != "\r") {
            append(current)
            current = readChar()
        }
    }
}

/**
 * Iterates over the stream and calls the specified [block] for each line of string.
 * A line is separated by a carriage return or a line feed.
 * The returned string does not contain the line separator.
 *
 * @param charset The charset to use.
 * @param block The block to call for each line.
 */
fun ReadStream.forEachLine(charset: Charset = Charsets.UTF8, block: (String) -> Unit) {
    while (availableBytes > 0) {
        val line = readLine(charset)
        if (line.isNotBlank())
            block(line)
    }
}

/**
 * Reads all lines of string from the stream with the specified [charset].
 * A line is separated by a carriage return or a line feed.
 * The returned string does not contain the line separator.
 * If the stream is empty, returns an empty list.
 * If the stream is not empty, returns a list containing all the lines.
 *
 * @param charset The charset to use.
 *
 * @return the lines read from the stream.
 */
fun ReadStream.readLines(charset: Charset = Charsets.UTF8): List<String> {
    val lines = arrayListOf<String>()
    forEachLine(charset) {
        lines += it
    }
    return lines
}
