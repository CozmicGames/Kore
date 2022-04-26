package com.cozmicgames.memory

import com.cozmicgames.utils.Disposable

/**
 * A buffer backed by [Memory] that can be used to store data of type [T].
 * Buffers must be disposed of when they are no longer needed.
 *
 * @param T The type of data stored in the buffer.
 *
 * Use the implementation classes below to create a buffer of a specific type.
 * @see [ByteBuffer]
 * @see [ShortBuffer]
 * @see [IntBuffer]
 * @see [LongBuffer]
 * @see [FloatBuffer]
 */
sealed class Buffer<T : Any>(size: Int, val valueSize: Int, private val allocator: Allocator) : Iterable<T>, Disposable {
    /**
     * The size of the buffer in elements.
     */
    var size = size
        private set

    /**
     * The memory backing the buffer.
     */
    var memory = allocator(size * valueSize)
        private set

    /**
     * Ensures that the buffer has enough space to store [size] elements.
     * If the buffer is not large enough, it will be resized.
     *
     * @param size The number of elements to ensure there's size for.
     */
    fun ensureSize(size: Int) {
        if (size > this.size)
            resize(size)
    }

    /**
     * Resizes the buffer to [size] elements.
     * If the size is the same as the current size, nothing will happen.
     * Data will be preserved.
     *
     * @param size The new size of the buffer.
     */
    fun resize(size: Int) {
        if (this.size == size)
            return

        val memory = allocator(size * valueSize)
        this.memory.copyTo(memory, kotlin.math.min(this.memory.size, memory.size))
        this.memory.dispose()
        this.memory = memory
        this.size = size
    }

    /**
     * Gets an iterator for the buffer.
     * This will iterate over the buffer in order.
     * The iterator will be invalidated if the buffer is resized or disposed.
     *
     * @return An iterator for the buffer.
     */
    abstract override fun iterator(): Iterator<T>

    /**
     * Gets the value at the given index.
     * If the index is out of bounds, an exception will be thrown.
     *
     * @param index The index of the value to get.
     *
     * @return The value at the given index.
     */
    abstract operator fun get(index: Int): T

    /**
     * Sets the [value] at the given [index].
     * If the index is out of bounds, an exception will be thrown.
     *
     * @param index The index of the value to set.
     * @param value The value to set.
     */
    abstract operator fun set(index: Int, value: T)

    /**
     * Disposes the buffer.
     * This will free the memory backing the buffer.
     */
    override fun dispose() {
        memory.dispose()
    }
}

/**
 * A buffer backed by [Memory] that can be used to store data of type [Byte].
 *
 * @param size The size of the buffer in elements.
 * @param allocator The allocator to use to allocate the memory. Defaults to [alloc].
 */
class ByteBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Byte>(size, Memory.SIZEOF_BYTE, allocator) {
    /**
     * Creates a new [ByteBuffer] with the given [size].
     * If [isStackAllocated], the memory backing the buffer will be allocated using [stackAlloc].
     * Otherwise, it will be allocated using [alloc].
     *
     * @param size The size of the buffer in elements.
     * @param isStackAllocated Whether the memory backing the buffer should be allocated using [stackAlloc].
     */
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Byte> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@ByteBuffer[index++]
    }

    override fun get(index: Int): Byte {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        return memory.getByte(index * valueSize)
    }

    override fun set(index: Int, value: Byte) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        memory.setByte(index * valueSize, value)
    }
}

/**
 * A buffer backed by [Memory] that can be used to store data of type [Short].
 *
 * @param size The size of the buffer in elements.
 * @param allocator The allocator to use to allocate the memory. Defaults to [alloc].
 */
class ShortBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Short>(size, Memory.SIZEOF_SHORT, allocator) {
    /**
     * Creates a new [ShortBuffer] with the given [size].
     * If [isStackAllocated], the memory backing the buffer will be allocated using [stackAlloc].
     * Otherwise, it will be allocated using [alloc].
     *
     * @param size The size of the buffer in elements.
     * @param isStackAllocated Whether the memory backing the buffer should be allocated using [stackAlloc].
     */
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Short> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@ShortBuffer[index++]
    }

    override fun get(index: Int): Short {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        return memory.getShort(index * valueSize)
    }

    override fun set(index: Int, value: Short) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        memory.setShort(index * valueSize, value)
    }
}

/**
 * A buffer backed by [Memory] that can be used to store data of type [Int].
 *
 * @param size The size of the buffer in elements.
 * @param allocator The allocator to use to allocate the memory. Defaults to [alloc].
 */
class IntBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Int>(size, Memory.SIZEOF_INT, allocator) {
    /**
     * Creates a new [IntBuffer] with the given [size].
     * If [isStackAllocated], the memory backing the buffer will be allocated using [stackAlloc].
     * Otherwise, it will be allocated using [alloc].
     *
     * @param size The size of the buffer in elements.
     * @param isStackAllocated Whether the memory backing the buffer should be allocated using [stackAlloc].
     */
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Int> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@IntBuffer[index++]
    }

    override fun get(index: Int): Int {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        return memory.getInt(index * valueSize)
    }

    override fun set(index: Int, value: Int) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        memory.setInt(index * valueSize, value)
    }
}

/**
 * A buffer backed by [Memory] that can be used to store data of type [Long].
 *
 * @param size The size of the buffer in elements.
 * @param allocator The allocator to use to allocate the memory. Defaults to [alloc].
 */
class LongBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Long>(size, Memory.SIZEOF_LONG, allocator) {
    /**
     * Creates a new [LongBuffer] with the given [size].
     * If [isStackAllocated], the memory backing the buffer will be allocated using [stackAlloc].
     * Otherwise, it will be allocated using [alloc].
     *
     * @param size The size of the buffer in elements.
     * @param isStackAllocated Whether the memory backing the buffer should be allocated using [stackAlloc].
     */
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Long> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@LongBuffer[index++]
    }

    override fun get(index: Int): Long {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        return memory.getLong(index * valueSize)
    }

    override fun set(index: Int, value: Long) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        memory.setLong(index * valueSize, value)
    }
}

/**
 * A buffer backed by [Memory] that can be used to store data of type [Float].
 *
 * @param size The size of the buffer in elements.
 * @param allocator The allocator to use to allocate the memory. Defaults to [alloc].
 */
class FloatBuffer(size: Int, allocator: Allocator = ::alloc) : Buffer<Float>(size, Memory.SIZEOF_FLOAT, allocator) {
    constructor(size: Int, isStackAllocated: Boolean) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc)

    override fun iterator() = object : Iterator<Float> {
        private var index = 0
        override fun hasNext() = index < size
        override fun next() = this@FloatBuffer[index++]
    }

    override fun get(index: Int): Float {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        return memory.getFloat(index * valueSize)
    }

    override fun set(index: Int, value: Float) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")

        memory.setFloat(index * valueSize, value)
    }
}
