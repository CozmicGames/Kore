package com.gratedgames.memory

import com.gratedgames.utils.Disposable

class StructArray<T : Struct>(val size: Int, supplier: () -> T) : Iterable<T>, Disposable {
    constructor(size: Int, allocator: Allocator, supplier: () -> T) : this(size, supplier) {
        internalMemorySupplier = { allocator(size) }
    }

    constructor(size: Int, isStackAllocated: Boolean, supplier: () -> T) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc, supplier)

    internal var getOffset: () -> Int = { 0 }

    private var ownsMemory = false
    private val structs = Array<Struct>(size) { supplier() }

    val structSize get() = structs.first().size

    val totalSize get() = size * structSize

    private val memorySupplier get() = requireNotNull(internalMemorySupplier)

    internal var internalMemorySupplier: (() -> Memory)? = null
        get() {
            if (field == null) {
                field = { alloc(totalSize) }
                ownsMemory = true
            }
            return field
        }

    val memory get() = requireNotNull(internalMemory)

    private var internalMemory: Memory? = null
        get() {
            if (field == null)
                field = memorySupplier()
            return field
        }

    init {
        require(size > 0)
        structs.forEachIndexed { index, struct ->
            struct.internalMemorySupplier = { memory }
            struct.getOffset = { getOffset() + index * structSize }
        }
    }

    override fun iterator() = structs.iterator() as Iterator<T>

    operator fun get(index: Int) = structs[index] as T

    override fun dispose() {
        internalMemory?.dispose()
    }
}