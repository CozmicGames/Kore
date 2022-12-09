package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.Disposable

interface WriteStream : Disposable {
    /**
     * The number of bytes written to the stream.
     */
    val writtenBytes: Int

    /**
     * Write the specified [values] from [offset] to [offset] + [length] to the stream.
     *
     * @param values The values to write.
     * @param offset The offset to start writing from.
     * @param length The number of values to write. Defaults to the length of [values] - [offset].
     */
    fun writeBytes(values: ByteArray, offset: Int = 0, length: Int = values.size - offset)

    /**
     * Write a single byte to the stream.
     *
     * @param value The byte to write.
     */
    fun writeByte(value: Byte)

    /**
     * Write a single short to the stream.
     *
     * @param value The short to write.
     */
    fun writeShort(value: Short)

    /**
     * Write a single int to the stream.
     *
     * @param value The int to write.
     */
    fun writeInt(value: Int)

    /**
     * Write a single float to the stream.
     *
     * @param value The float to write.
     */
    fun writeFloat(value: Float)

    /**
     * Write a single string to the stream in the specified [charset].
     *
     * @param value The string to write.
     * @param charset The charset to use when writing the string. Defaults to UTF-8.
     */
    fun writeString(value: String, charset: Charset = Charsets.UTF8)
}

/**
 * Writes an 8-bit integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt8(value: Int) = writeByte((value and 0xFF).toByte())

/**
 * Writes an 16-bit little endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt16LE(value: Int) {
    writeInt8(value and 0xFF)
    writeInt8((value ushr 8) and 0xFF)
}

/**
 * Writes an 24-bit little endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt24LE(value: Int) {
    writeInt8(value and 0xFF)
    writeInt8((value ushr 8) and 0xFF)
    writeInt8((value ushr 16) and 0xFF)
}

/**
 * Writes an 32-bit little endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt32LE(value: Int) {
    writeInt8(value and 0xFF)
    writeInt8((value ushr 8) and 0xFF)
    writeInt8((value ushr 16) and 0xFF)
    writeInt8((value ushr 24) and 0xFF)
}

/**
 * Writes an 64-bit little endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt64LE(value: Long) {
    writeInt32LE(value.toInt())
    writeInt32LE((value ushr 32).toInt())
}

/**
 * Writes an 32-bit little endian float to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeFloat32LE(value: Float) = writeInt32LE(value.toRawBits())

/**
 * Writes an 64-bit little endian float to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeFloat64LE(value: Double) = writeInt64LE(value.toRawBits())

/**
 * Writes an 16-bit big endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt16BE(value: Int) {
    writeInt8((value ushr 8) and 0xFF)
    writeInt8(value and 0xFF)
}

/**
 * Writes an 24-bit big endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt24BE(value: Int) {
    writeInt8((value ushr 16) and 0xFF)
    writeInt8((value ushr 8) and 0xFF)
    writeInt8(value and 0xFF)
}

/**
 * Writes an 32-bit big endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt32BE(value: Int) {
    writeInt8((value ushr 24) and 0xFF)
    writeInt8((value ushr 16) and 0xFF)
    writeInt8((value ushr 8) and 0xFF)
    writeInt8(value and 0xFF)
}

/**
 * Writes an 64-bit big endian integer to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeInt64BE(value: Long) {
    writeInt32BE((value ushr 32).toInt())
    writeInt32BE(value.toInt())
}

/**
 * Writes an 32-bit big endian float to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeFloat32BE(value: Float) = writeInt32BE(value.toRawBits())

/**
 * Writes an 64-bit big endian float to the stream.
 *
 * @param value The value to write.
 */
fun WriteStream.writeFloat64BE(value: Double) = writeInt64BE(value.toRawBits())

/**
 * Writes an 16-bit integer to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeInt16(value: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt16LE(value)
    Files.Endianness.BIG_ENDIAN -> writeInt16BE(value)
}

/**
 * Writes an 24-bit integer to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeInt24(value: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt24LE(value)
    Files.Endianness.BIG_ENDIAN -> writeInt24BE(value)
}

/**
 * Writes an 32-bit integer to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeInt32(value: Int, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt32LE(value)
    Files.Endianness.BIG_ENDIAN -> writeInt32BE(value)
}

/**
 * Writes an 64-bit integer to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeInt64(value: Long, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeInt64LE(value)
    Files.Endianness.BIG_ENDIAN -> writeInt64BE(value)
}

/**
 * Writes an 32-bit float to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeFloat32(value: Float, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeFloat32LE(value)
    Files.Endianness.BIG_ENDIAN -> writeFloat32BE(value)
}

/**
 * Writes an 64-bit float to the stream.
 *
 * @param value The value to write.
 * @param endianness The endianness to use when writing the value. Defaults to the platforms' native endianness.
 */
fun WriteStream.writeFloat64(value: Double, endianness: Files.Endianness = Kore.files.nativeEndianness) = when (endianness) {
    Files.Endianness.LITTLE_ENDIAN -> writeFloat64LE(value)
    Files.Endianness.BIG_ENDIAN -> writeFloat64BE(value)
}

/**
 * Writes a line of string to the stream in the specified [charset].
 *
 * @param line The line to write.
 * @param charset The charset to use when writing the line.
 */
fun WriteStream.writeLine(line: String, charset: Charset = Charsets.UTF8) = writeString("$line\n", charset)
