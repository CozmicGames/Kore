package com.gratedgames.memory

import com.gratedgames.utils.Disposable

sealed class Buffer<T : Any>(size: Int, val valueSize: Int, private val allocator: Allocator) : Iterable<T>, Disposable {
    var size = size
        private set

    var memory = allocator(size * valueSize)
        private set

    fun ensureSize(size: Int) {
        if (size > this.size)
            resize(size)
    }

    fun resize(size: Int) {
        if (this.size == size)
            return

        val memory = allocator(size * valueSize)
        this.memory.copyTo(memory, kotlin.math.min(this.memory.size, memory.size))
        this.memory.dispose()
        this.memory = memory
        this.size = size
    }

    abstract operator fun get(index: Int): T

    abstract operator fun set(index: Int, value: T)

    override fun dispose() {
        memory.dispose()
    }
}

class ByteBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Byte>(size, Memory.SIZEOF_BYTE, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Byte> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@ByteBuffer[index++]
    }

    override fun get(index: Int) = memory.getByte(index * valueSize)

    override fun set(index: Int, value: Byte) = memory.setByte(index * valueSize, value)
}

class ShortBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Short>(size, Memory.SIZEOF_SHORT, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Short> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@ShortBuffer[index++]
    }

    override fun get(index: Int) = memory.getShort(index * valueSize)

    override fun set(index: Int, value: Short) = memory.setShort(index * valueSize, value)
}

class IntBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Int>(size, Memory.SIZEOF_INT, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Int> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@IntBuffer[index++]
    }

    override fun get(index: Int) = memory.getInt(index * valueSize)

    override fun set(index: Int, value: Int) = memory.setInt(index * valueSize, value)
}

class LongBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Long>(size, Memory.SIZEOF_LONG, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Long> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@LongBuffer[index++]
    }

    override fun get(index: Int) = memory.getLong(index * valueSize)

    override fun set(index: Int, value: Long) = memory.setLong(index * valueSize, value)
}

class FloatBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Float>(size, Memory.SIZEOF_FLOAT, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Float> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@FloatBuffer[index++]
    }

    override fun get(index: Int) = memory.getFloat(index * valueSize)

    override fun set(index: Int, value: Float) = memory.setFloat(index * valueSize, value)
}
