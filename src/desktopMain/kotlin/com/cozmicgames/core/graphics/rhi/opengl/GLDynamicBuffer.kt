package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUDynamicBuffer
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL45C.glNamedBufferStorage
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryUtil.*

class GLDynamicBuffer(val device: GLDevice) : GPUDynamicBuffer(), GLBuffer {
    override var handle: Int = 0
    private var size = 0
    private var mappedAddress = 0L
    private var dataAddress: Long? = null
    private var isDataValid = false

    private fun ensureIsCreated() {
        if (handle == 0) {
            handle = glCreateBuffers()
            DesktopStatistics.numBuffers++
        }
    }

    override fun setDataSource(memory: Memory, offset: Int, size: Int) {
        ensureIsCreated()

        val flags = GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT

        glNamedBufferStorage(handle, size.toLong(), flags)
        mappedAddress = nglMapNamedBufferRange(handle, 0, size.toLong(), flags)

        check(mappedAddress != 0L) { "Failed to map OpenGL dynamic buffer" }

        this.size = size
        device.usedMemoryInternal += size

        dataAddress = memory.address + offset

        isDataValid = false
    }

    override fun invalidateData() {
        isDataValid = false
    }

    internal fun updateDataIfNecessary() {
        if (isDataValid)
            return

        val dataAddress = dataAddress ?: return
        memCopy(dataAddress, mappedAddress, size.toLong())
        isDataValid = true
    }

    override fun dispose() {
        if (handle != 0) {
            if (mappedAddress != 0L) {
                glUnmapNamedBuffer(handle)
                mappedAddress = 0L
            }

            glDeleteBuffers(handle)

            device.usedMemoryInternal -= size

            handle = 0
            size = 0
            DesktopStatistics.numBuffers--
        }
    }
}