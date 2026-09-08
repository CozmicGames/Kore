package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.TextureUtils
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.rhi.GPUTexture1D
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.internal.checkError
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL46C.*

class GLTexture1D(private val device: GLDevice, format: GPUTextureFormat) : GPUTexture1D(format), GLTexture {
    override var handle = 0
    private var sizeInternal = 0
    private var mipLevels = 0

    override val size: Int get() = sizeInternal

    private fun calculateMemorySize(): Long = TextureUtils.memorySize(sizeInternal, 1, 1, format, mipLevels)

    private fun create(size: Int, withMipLevels: Boolean) {
        mipLevels = if (withMipLevels) TextureUtils.mipLevels(size) else 1
        handle = glCreateTextures(GL_TEXTURE_1D)
        glTextureStorage1D(handle, mipLevels, format.toGLFormat(), size)
        sizeInternal = size
        device.usedMemoryInternal += calculateMemorySize()
        DesktopStatistics.numTextures++
    }

    private fun recreate(size: Int, withMipLevels: Boolean) {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
        }

        create(size, withMipLevels)
    }

    override fun setSize(size: Int, withMipLevels: Boolean) {
        device.checkError(size > 0) { "Texture size must be > 0" }

        if (size <= 0)
            return

        if (handle == 0)
            create(size, withMipLevels)
        else if (size != sizeInternal)
            recreate(size, withMipLevels)
    }

    override fun setImage(data: Memory, dataFormat: GPUTextureFormat, offset: Int, level: Int) {
        device.checkFail(handle != 0) { "Texture must be created first using setSize()" }
        device.checkError(size > 0) { "Texture size must be > 0" }
        device.checkError(level >= 0) { "Mip level must be >= 0" }
        device.checkError(offset >= 0) { "Offset must be >= 0" }

        if (size <= 0) return

        val mipSize = TextureUtils.mipSize(sizeInternal, level)

        device.checkError(level < mipLevels) { "Invalid mip level: $level" }
        device.checkError(offset + size <= mipSize) { "Image exceeds mip level size" }

        if (level < mipLevels && offset >= 0 && offset + size <= mipSize)
            nglTextureSubImage1D(handle, level, offset, size, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
    }

    override fun compareTo(other: GPUTexture): Int {
        if (this === other) return 0
        if (other !is GLTexture1D) return 1
        return handle.compareTo(other.handle)
    }

    override fun dispose() {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
            handle = 0
            sizeInternal = 0
            mipLevels = 0
            DesktopStatistics.numTextures--
        }
    }
}