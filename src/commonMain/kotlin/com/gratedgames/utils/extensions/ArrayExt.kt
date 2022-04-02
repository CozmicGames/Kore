package com.gratedgames.utils.extensions

import com.gratedgames.utils.ByteArrayBuilder
import com.gratedgames.utils.hashCodeOf
import kotlin.reflect.KProperty

inline fun <T> Array<T>.indexOf(element: T): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun ByteArray.indexOf(element: Byte): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun CharArray.indexOf(element: Char): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun ShortArray.indexOf(element: Short): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun IntArray.indexOf(element: Int): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun LongArray.indexOf(element: Long): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun FloatArray.indexOf(element: Float): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun DoubleArray.indexOf(element: Double): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun BooleanArray.indexOf(element: Boolean): Int? {
    for (i in indices)
        if (this[i] == element)
            return i
    return null
}

inline fun <T> Array<T>.fill(element: T, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun ByteArray.fill(element: Byte, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun CharArray.fill(element: Char, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun ShortArray.fill(element: Short, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun IntArray.fill(element: Int, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun LongArray.fill(element: Long, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun FloatArray.fill(element: Float, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun DoubleArray.fill(element: Double, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun BooleanArray.fill(element: Boolean, start: Int = 0, end: Int = size) {
    for (i in (start until end))
        this[i] = element
}

inline fun read8(index: Int, access: (Int) -> Byte) = access(index).toInt() and 0xFF

inline fun read16(index: Int, access: (Int) -> Byte): Int {
    return read8(index, access) or (read8(index + 1, access) shl 8)
}

inline fun read32(index: Int, access: (Int) -> Byte): Int {
    return read16(index, access) or (read16(index + 2, access) shl 16)
}

inline fun read64(index: Int, access: (Int) -> Byte): Long {
    return read32(index, access).toLong() or (read32(index + 4, access).toLong() shl 32)
}

inline fun write8(index: Int, value: Int, access: (Int, Byte) -> Unit) = access(index, (value and 0xFF).toByte())

inline fun write16(index: Int, value: Int, access: (Int, Byte) -> Unit) {
    write8(index, (value and 0xFF), access)
    write8(index + 1, ((value and 0xFF00) shr 8), access)
}

inline fun write32(index: Int, value: Int, access: (Int, Byte) -> Unit) {
    write16(index, (value and 0xFFFF), access)
    write16(index + 2, ((value and 0xFFFF0000.toInt()) ushr 32), access)
}

inline fun write64(index: Int, value: Long, access: (Int, Byte) -> Unit) {
    write32(index, (value and 0xFFFFFFFFL).toInt(), access)
    write32(index + 2, ((value and (0xFFFFFFFF shl 32)).toInt() ushr 32), access)
}

inline fun ByteArray.read8(index: Int) = read8(index) { this[it] }

inline fun ByteArray.read16(index: Int) = read16(index) { this[it] }

inline fun ByteArray.read32(index: Int) = read32(index) { this[it] }

inline fun ByteArray.write8(index: Int, value: Int) = write8(index, value) { i, v -> this[i] = v }

inline fun ByteArray.write16(index: Int, value: Int) = write16(index, value) { i, v -> this[i] = v }

inline fun ByteArray.write32(index: Int, value: Int) = write32(index, value) { i, v -> this[i] = v }

inline fun ByteArray.write64(index: Int, value: Long) = write64(index, value) { i, v -> this[i] = v }

class ArrayElement<T>(val array: Array<T>, val index: Int) {
    operator fun getValue(thisRef: Any, property: KProperty<*>) = array[index]
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        array[index] = value
    }
}

fun <T> Array<T>.element(index: Int) = ArrayElement(this, index)

val EmptyIntArray = IntArray(0)

fun emptyIntArray() = EmptyIntArray

val EmptyFloatArray = FloatArray(0)

fun emptyFloatArray() = EmptyFloatArray

fun ByteArray.encodeRunLength() = ByteArrayBuilder().also { encodeRunLength(it) }.toByteArray()

fun ByteArray.encodeRunLength(builder: ByteArrayBuilder) = encodeRunLength { count, value ->
    builder.append(count, value)
}

fun ByteArray.encodeRunLength(block: (Byte, Byte) -> Unit) {
    if (isEmpty())
        return

    var index = 0
    while (index < size) {
        var count = 0
        val value = this[index]

        while (index < size && this[index] == value && count < Byte.MAX_VALUE) {
            count++
            index++
        }

        block(count.toByte(), value)

        index++
    }
}

fun ByteArray.decodeRunLength() = ByteArrayBuilder().also { decodeRunLength(it) }.toByteArray()

fun ByteArray.decodeRunLength(builder: ByteArrayBuilder) = decodeRunLength { builder.append(it) }

fun ByteArray.decodeRunLength(block: (Byte) -> Unit) {
    if (isEmpty())
        return

    var index = 0
    while (index < size) {
        val count = this[index]
        val value = this[index + 1]

        repeat(count.toInt()) {
            block(value)
        }

        index += 2
    }
}

fun <T : Any> Array<T>.contentHashCode() = hashCodeOf(*this)

fun <T : Any> Array<T>.swap(indexA: Int, indexB: Int) {
    val temp = this[indexA]
    this[indexA] = this[indexB]
    this[indexB] = temp
}

@kotlin.jvm.JvmName("sumOfFloat")
inline fun <T> Array<out T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0.0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}