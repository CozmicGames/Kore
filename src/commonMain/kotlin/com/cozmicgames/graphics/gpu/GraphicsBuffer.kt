package com.cozmicgames.graphics.gpu

import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.Disposable
import kotlin.math.min

abstract class GraphicsBuffer(val usage: Usage) : Disposable {
    enum class Usage {
        STATIC,
        DYNAMIC
    }

    enum class MapAccess {
        READ,
        WRITE,
        READ_WRITE
    }

    abstract val size: Int

    abstract fun setSize(size: Int)
    abstract fun setData(data: Memory, offset: Int = 0, size: Int = data.size)
    abstract fun setSubData(offset: Int, data: Memory, dataOffset: Int = 0, size: Int = data.size)
    abstract fun getData(data: Memory, offset: Int = 0, size: Int = min(this.size, data.size))
    abstract fun map(access: MapAccess, offset: Int = 0, size: Int = this.size): Memory
    abstract fun flushMappedRange(offset: Int = 0, size: Int = this.size)
    abstract fun unmap()
}