package com.cozmicgames.memory

import com.cozmicgames.utils.Disposable
import kotlin.math.min

/**
 * A struct buffer is a resizable collection of structs that can be accessed by index.
 * The structs in the buffer use a shared [Memory] instance to store their data.
 * They are in a contiguous block of memory and can be accessed by index.
 * Struct buffers must be disposed of when no longer needed.
 *
 * @param T The struct type.
 * @param size The initial size of the struct buffer.
 * @param allocator The allocator used to allocate the backing memory.
 * @param supplier The supplier that will be used to create the structs. Structs initialized by the supplier must be created without an allocator.
 */
class StructBuffer<T : Struct>(size: Int, private val allocator: Allocator, private val supplier: () -> T) : Iterable<T>, Disposable {
    /**
     * Creates a new struct buffer with the specified initial size. Structs will be allocated by [stackAlloc] or [alloc], depending on [isStackAllocated].
     *
     * @param size The initial size of the struct buffer.
     * @param isStackAllocated Whether the structs will be allocated by [stackAlloc] or [alloc].
     * @param supplier The supplier that will be used to create the structs. Structs initialized by the supplier must be created without an allocator.
     */
    constructor(size: Int, isStackAllocated: Boolean, supplier: () -> T) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc, supplier)

    private var structs = Array<Struct>(size) { supplier() }

    /**
     * The size of a single struct in the array, in bytes.
     */
    val structSize get() = structs.first().size

    /**
     * The total size of all structs in the array, in bytes.
     */
    val totalSize get() = size * structSize

    /**
     * The memory used by the structs in the array.
     */
    var memory = allocator(size * structSize)
        private set

    /**
     * The number of structs in the buffer.
     */
    var size = size
        private set

    init {
        resetStructs()
    }

    private fun resetStructs() {
        structs.forEachIndexed { index, struct ->
            struct.internalMemorySupplier = { memory }
            struct.getOffset = { index * structSize }
        }
    }

    /**
     * Ensures that the buffer has enough space to store [size] structs.
     * If the buffer is not large enough, it will be resized.
     *
     * @param size The number of structs to ensure there's size for.
     */
    fun ensureSize(size: Int) {
        if (size > this.size)
            resize(size)
    }

    /**
     * Resizes the buffer to [size] structs.
     * If the size is the same as the current size, nothing will happen.
     * Data stored in the backing memory will be preserved.
     * Other data will be lost.
     *
     * @param size The new size of the buffer.
     */
    fun resize(size: Int) {
        if (this.size == size)
            return

        val newMemory = allocator(structSize * size)
        memory.copyTo(newMemory, min(this.memory.size, newMemory.size))
        memory.dispose()
        memory = newMemory

        structs = Array(size) { supplier() }
        resetStructs()

        this.size = size
    }

    /**
     * Gets an iterator for the buffer.
     * This will iterate over the buffer in order.
     * The iterator will be invalidated if the buffer is resized or disposed.
     *
     * @return An iterator for the buffer.
     */
    @Suppress("UNCHECKED_CAST")
    override fun iterator() = structs.iterator() as Iterator<T>

    /**
     * Gets the struct at the given index.
     * If the index is out of bounds, an exception will be thrown.
     *
     * @param index The index of the struct to get.
     *
     * @return The struct at the given index.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int) = structs[index] as T

    /**
     * Disposes the buffer.
     * This will free the memory backing the buffer.
     */
    override fun dispose() {
        memory.dispose()
    }
}