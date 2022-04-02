package com.gratedgames.utils

import com.gratedgames.utils.extensions.write16
import com.gratedgames.utils.extensions.write32
import com.gratedgames.utils.extensions.write8
import kotlin.math.max

fun buildByteArray(capacity: Int = ByteArrayBuilder.DEFAULT_SIZE, callback: ByteArrayBuilder.() -> Unit): ByteArray = ByteArrayBuilder(capacity).apply(callback).toByteArray()

class ByteArrayBuilder(var data: ByteArray, private var pointer: Int = data.size, private val allowGrow: Boolean = true) {
    companion object{
        internal const val DEFAULT_SIZE = 4096
    }

    constructor(initialCapacity: Int = DEFAULT_SIZE, allowGrow: Boolean = true) : this(ByteArray(initialCapacity), 0, allowGrow)

    var size: Int
        get() = pointer
        set(value) {
            val oldPosition = pointer
            ensure(value)
            pointer = value
            if (value > oldPosition)
                data.fill(0, oldPosition, value)
        }

    private fun ensure(required: Int) {
        if (data.size < required) {
            require(allowGrow)
            data = data.copyOf(max(required, (data.size + 7) * 5))
        }
    }

    private inline fun <T> prepare(count: Int, callback: () -> T): T {
        ensure(pointer + count)
        return callback().also { pointer += count }
    }

    fun append8(value: Int) = prepare(1) {
        data.write8(pointer, value)
    }

    fun append16(value: Int) = prepare(2) {
        data.write16(pointer, value)
    }

    fun append32(value: Int) = prepare(4) {
        data.write32(pointer, value)
    }

    fun append(vararg values: Byte) = values.forEach { append8(it.toInt()) }

    fun append(vararg values: Char) = values.forEach { append16(it.code) }

    fun append(vararg values: Short) = values.forEach { append16(it.toInt()) }

    fun append(vararg values: Int) = values.forEach { append32(it) }

    fun append(vararg values: Float) = values.forEach { append32(it.toBits()) }

    fun clear() {
        pointer = 0
    }

    fun toByteArray(): ByteArray = data.copyOf(pointer)
}