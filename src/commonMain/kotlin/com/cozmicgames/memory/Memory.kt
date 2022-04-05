package com.cozmicgames.memory

import com.cozmicgames.Kore
import com.cozmicgames.memoryAccess
import com.cozmicgames.utils.Disposable

class Memory private constructor(val address: Long, val size: Int, private val offset: Int, val isPartition: Boolean = false) : Disposable {
    constructor(size: Int) : this(Kore.memoryAccess.alloc(size), size, 0)

    companion object {
        const val SIZEOF_BYTE = 1
        const val SIZEOF_SHORT = 2 * SIZEOF_BYTE
        const val SIZEOF_INT = 4 * SIZEOF_BYTE
        const val SIZEOF_LONG = 8 * SIZEOF_BYTE
        const val SIZEOF_FLOAT = SIZEOF_INT

        val total get() = Kore.memoryAccess.totalMemory
        val free get() = Kore.memoryAccess.freeMemory
        val used get() = Kore.memoryAccess.usedMemory
    }

    fun fill(value: Byte, offset: Int = 0, size: Int = this.size - offset) = Kore.memoryAccess.set(address, offset, value, size)

    fun getByte(position: Int) = Kore.memoryAccess.getByte(address, position + offset)

    fun getShort(position: Int) = Kore.memoryAccess.getShort(address, position + offset)

    fun getInt(position: Int) = Kore.memoryAccess.getInt(address, position + offset)

    fun getLong(position: Int) = Kore.memoryAccess.getLong(address, position + offset)

    fun getFloat(position: Int) = Float.fromBits(getInt(position))

    fun setByte(position: Int, value: Byte) = Kore.memoryAccess.putByte(address, position + offset, value)

    fun setShort(position: Int, value: Short) = Kore.memoryAccess.putShort(address, position + offset, value)

    fun setInt(position: Int, value: Int) = Kore.memoryAccess.putInt(address, position + offset, value)

    fun setLong(position: Int, value: Long) = Kore.memoryAccess.putLong(address, position + offset, value)

    fun setFloat(position: Int, value: Float) = setInt(position, value.toBits())

    fun copyTo(memory: Memory, length: Int = size, offset: Int = 0, destOffset: Int = 0) {
        require(memory.size - destOffset >= length)
        Kore.memoryAccess.copy(address, this.offset + offset, memory.address, destOffset, length)
    }

    fun partition(offset: Int, size: Int) = Memory(address, size, offset, true)

    override fun dispose() {
        if (isPartition)
            return

        Kore.memoryAccess.free(address)
    }
}

fun Memory.Companion.of(vararg values: Byte) = Memory(values.size * SIZEOF_BYTE).also {
    values.forEachIndexed { index, value ->
        it.setByte(index * SIZEOF_BYTE, value)
    }
}

fun Memory.Companion.of(vararg values: Short) = Memory(values.size * SIZEOF_SHORT).also {
    values.forEachIndexed { index, value ->
        it.setShort(index * SIZEOF_SHORT, value)
    }
}

fun Memory.Companion.of(vararg values: Int) = Memory(values.size * SIZEOF_INT).also {
    values.forEachIndexed { index, value ->
        it.setInt(index * SIZEOF_INT, value)
    }
}

fun Memory.Companion.of(vararg values: Long) = Memory(values.size * SIZEOF_LONG).also {
    values.forEachIndexed { index, value ->
        it.setLong(index * SIZEOF_LONG, value)
    }
}

fun Memory.Companion.of(vararg values: Float) = Memory(values.size * SIZEOF_FLOAT).also {
    values.forEachIndexed { index, value ->
        it.setFloat(index * SIZEOF_FLOAT, value)
    }
}

fun <R> Memory.Companion.scope(block: () -> R): R {
    MemoryStack.push()
    return try {
        block()
    } finally {
        MemoryStack.pop()
    }
}

fun Memory.clear(offset: Int = 0, size: Int = this.size) = fill(0, offset, size)

fun alloc(size: Int) = Memory(size)

fun stackAlloc(size: Int) = MemoryStack.alloc(size)

val Int.bytes get() = this

val Int.kilobytes get() = bytes shl 10

val Int.megabytes get() = kilobytes shl 10

val Int.gigabytes get() = megabytes shl 10

typealias Allocator = (Int) -> Memory