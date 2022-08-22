package com.cozmicgames.memory

import com.cozmicgames.utils.Disposable

/**
 * A struct array is a collection of structs that can be accessed by index.
 * The structs in the array use a shared [Memory] instance to store their data.
 * They are in a contiguous block of memory and can be accessed by index.
 * Struct arrays must be disposed of when no longer needed.
 *
 * @param T The struct type.
 * @param size The size of the struct array.
 * @param supplier The supplier that will be used to create the structs. Structs initialized by the supplier must be created without an allocator.
 */
class StructArray<T : Struct>(val size: Int, supplier: () -> T) : Iterable<T>, Disposable {
    /**
     * Creates a new struct array with the specified size and allocator.
     *
     * @param size The size of the struct array.
     * @param allocator The allocator used to allocate the backing memory.
     * @param supplier The supplier that will be used to create the structs. Structs initialized by the supplier must be created without an allocator.
     */
    constructor(size: Int, allocator: Allocator, supplier: () -> T) : this(size, supplier) {
        internalMemorySupplier = { allocator(size) }
    }

    /**
     * Creates a new struct array with the specified size. Structs will be allocated by [stackAlloc] or [alloc], depending on [isStackAllocated].
     *
     * @param size The size of the struct array.
     * @param isStackAllocated Whether the structs will be allocated by [stackAlloc] or [alloc].
     * @param supplier The supplier that will be used to create the structs. Structs initialized by the supplier must be created without an allocator.
     */
    constructor(size: Int, isStackAllocated: Boolean, supplier: () -> T) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc, supplier)

    private var ownsMemory = false
    private val structs = Array<Struct>(size) { supplier() }
    private val memorySupplier get() = requireNotNull(internalMemorySupplier)

    internal var getOffset: () -> Int = { 0 }

    internal var internalMemorySupplier: (() -> Memory)? = null
        get() {
            if (field == null) {
                field = { alloc(totalSize) }
                ownsMemory = true
            }
            return field
        }

    private var internalMemory: Memory? = null
        get() {
            if (field == null)
                field = memorySupplier()
            return field
        }

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
    val memory get() = requireNotNull(internalMemory)

    init {
        require(size > 0)
        structs.forEachIndexed { index, struct ->
            struct.internalMemorySupplier = { memory }
            struct.getOffset = { getOffset() + index * structSize }
        }
    }

    /**
     * Gets an iterator for the array.
     * This will iterate over the structs in the array in order.
     * The iterator will be invalidated if the array is disposed.
     *
     * @return An iterator for the structs in the array.
     */
    @Suppress("UNCHECKED_CAST")
    override fun iterator() = structs.iterator() as Iterator<T>

    /**
     * Gets the struct at the specified index.
     *
     * @param index The index of the struct to get.
     *
     * @return The struct at the specified index.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int) = structs[index] as T

    /**
     * Disposes of the struct array.
     * This will dispose of the memory used by the structs in the array.
     */
    override fun dispose() {
        internalMemory?.dispose()
    }
}