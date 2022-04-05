package com.cozmicgames.memory

import com.cozmicgames.utils.Disposable
import kotlin.math.min

class StructBuffer<T : Struct>(size: Int, private val allocator: Allocator, private val supplier: () -> T) : Iterable<T>, Disposable {
    constructor(size: Int, isStackAllocated: Boolean, supplier: () -> T) : this(size, if (isStackAllocated) ::stackAlloc else ::alloc, supplier)

    private var structs = Array<Struct>(size) { supplier() }

    val structSize get() = structs.first().size

    var memory = allocator(size * structSize)
        private set

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

    fun ensureSize(size: Int) {
        if (size > this.size)
            resize(size)
    }

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

    override fun iterator() = structs.iterator() as Iterator<T>

    operator fun get(index: Int) = structs[index] as T

    override fun dispose() {
        memory.dispose()
    }
}