package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUDynamicBuffer
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.graphics.rhi.internal.checkError
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL45C.glNamedBufferStorage
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryUtil.*

class GLDynamicBuffer(val device: GLDevice) : GPUDynamicBuffer(), GLBuffer {
    override var handle: Int = 0

    override var size = 0
        private set

    private var mappedMemory: Memory? = null

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

        val flags = GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT

        glNamedBufferStorage(handle, size.toLong(), flags)
        val mappedAddress = nglMapNamedBufferRange(handle, 0, size.toLong(), flags)

        mappedMemory = Memory(mappedAddress, size)

        this.size = size
        device.usedMemoryInternal += size
    }

    override fun updateData(block: (Memory) -> Unit) {
        val mappedMemory = this.mappedMemory

        if (!device.checkError(mappedMemory != null) { "UpdateData requires setSize to be called first" })
            return

        block(mappedMemory!!)
    }

    override fun dispose() {
        if (handle != 0) {
            if (mappedMemory != null) {
                glUnmapNamedBuffer(handle)
                mappedMemory = null
            }

            glDeleteBuffers(handle)

            device.usedMemoryInternal -= size

            handle = 0
            size = 0
            DesktopStatistics.numBuffers--
        }
    }
}