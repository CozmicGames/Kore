package com.gratedgames.graphics.gpu

import com.gratedgames.memory.Memory
import com.gratedgames.utils.Disposable
import kotlin.math.min

abstract class GraphicsBuffer(val usage: Usage) : Disposable {
    enum class Usage {
        STATIC,
        DYNAMIC
    }

    abstract val size: Int

    abstract fun setSize(size: Int)
    abstract fun setData(data: Memory, offset: Int = 0, size: Int = data.size)
    abstract fun setSubData(offset: Int, data: Memory, dataOffset: Int = 0, size: Int = data.size)
    abstract fun getData(data: Memory, offset: Int = 0, size: Int = min(this.size, data.size))
}