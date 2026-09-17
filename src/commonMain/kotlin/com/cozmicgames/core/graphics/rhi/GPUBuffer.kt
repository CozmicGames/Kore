package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.memory.Memory
import com.cozmicgames.core.utils.Disposable

sealed interface GPUBuffer : Disposable {
    enum class Type {
        DYNAMIC,
        STATIC
    }

    val type: Type
    val size: Int

    fun setSize(size: Int)
}

/**
 * A buffer that can be updated dynamically.
 */
abstract class GPUDynamicBuffer : GPUBuffer {
    final override val type get() = GPUBuffer.Type.DYNAMIC

    abstract fun updateData(block: (Memory) -> Unit)
}

/**
 * A buffer that is updated once and then remains static.
 */
abstract class GPUStaticBuffer : GPUBuffer {
    final override val type get() = GPUBuffer.Type.STATIC

    abstract fun setData(memory: Memory, sourceOffset: Int = 0, destOffset: Int = 0, size: Int = memory.size - sourceOffset)
}
