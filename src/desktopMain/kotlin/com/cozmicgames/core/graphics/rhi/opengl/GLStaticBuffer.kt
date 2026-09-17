package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUStaticBuffer
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL45C.glNamedBufferStorage
import org.lwjgl.opengl.GL46C.*

class GLStaticBuffer(val device: GLDevice) : GPUStaticBuffer(), GLBuffer {
    override var handle: Int = 0

    override var size = 0
        private set

    override fun setSize(size: Int) {
        device.checkFail(size > 0) { "Buffer size must be > 0" }

        if (this.size == size)
            return

        if (handle != 0) {
            glDeleteBuffers(handle)
            device.usedMemoryInternal -= this.size
        }

        handle = glCreateBuffers()
        DesktopStatistics.numBuffers++

        glNamedBufferStorage(handle, size.toLong(), GL_DYNAMIC_STORAGE_BIT)

        this.size = size
        device.usedMemoryInternal += size
    }

    override fun setData(memory: Memory, sourceOffset: Int, destOffset: Int, size: Int) {
        device.checkFail(handle != 0) { "Buffer is not created" }
        device.checkFail(sourceOffset >= 0 && destOffset >= 0 && size >= 0) { "Invalid offset or size" }

        nglNamedBufferSubData(handle, destOffset.toLong(), size.toLong(), memory.address + sourceOffset)
    }

    override fun dispose() {
        if (handle != 0) {
            glDeleteBuffers(handle)
            device.usedMemoryInternal -= size
            handle = 0
            size = 0
            DesktopStatistics.numBuffers--
        }
    }
}