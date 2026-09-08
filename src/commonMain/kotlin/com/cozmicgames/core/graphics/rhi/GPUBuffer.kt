package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.memory.Memory
import com.cozmicgames.core.utils.Disposable

sealed interface GPUBuffer : Disposable {
    enum class Type {
        DYNAMIC,
        STATIC
    }

    val type: Type
}

/**
 * A buffer that can be updated dynamically.
 * Data is copied from a memory source to the buffer when needed.
 * This happens before the next draw call referencing this buffer after [setDataSource] or [invalidateData] is called.
 * If you want to update the buffer every frame, you can call [invalidateData] every frame to ensure the data is copied before the next draw call.
 */
abstract class GPUDynamicBuffer : GPUBuffer {
    final override val type get() = GPUBuffer.Type.DYNAMIC

    abstract fun setDataSource(memory: Memory, offset: Int = 0, size: Int = memory.size - offset)
    abstract fun invalidateData()
}

/**
 * A buffer that is updated once and then remains static.
 * Data is copied once from a memory source to the buffer when [setData] is called.
 * The buffer does not support dynamic updates and should be used for data that does not change frequently.
 */
abstract class GPUStaticBuffer : GPUBuffer {
    final override val type get() = GPUBuffer.Type.STATIC

    abstract fun setSize(size: Int)
    abstract fun setData(memory: Memory, sourceOffset: Int = 0, destOffset: Int = 0, size: Int = memory.size - sourceOffset)
}
