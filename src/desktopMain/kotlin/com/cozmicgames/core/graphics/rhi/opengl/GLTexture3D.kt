package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.TextureUtils
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.rhi.GPUTexture3D
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.internal.checkError
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL46C.*

class GLTexture3D(private val device: GLDevice, format: GPUTextureFormat) : GPUTexture3D(format), GLTexture {
    override var handle = 0
    private var widthInternal = 0
    private var heightInternal = 0
    private var depthInternal = 0
    private var mipLevels = 0

    override val width: Int get() = widthInternal
    override val height: Int get() = heightInternal
    override val depth: Int get() = depthInternal

    private fun calculateMemorySize(): Long = TextureUtils.memorySize(widthInternal, heightInternal, depthInternal, format, mipLevels)

    private fun create(width: Int, height: Int, depth: Int, withMipLevels: Boolean) {
        mipLevels = if (withMipLevels) TextureUtils.mipLevels(width, height, depth) else 1
        handle = glCreateTextures(GL_TEXTURE_3D)
        glTextureStorage3D(handle, mipLevels, format.toGLFormat(), width, height, depth)
        widthInternal = width
        heightInternal = height
        depthInternal = depth
        device.usedMemoryInternal += calculateMemorySize()
        DesktopStatistics.numTextures++
    }

    private fun recreate(width: Int, height: Int, depth: Int, withMipLevels: Boolean) {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
        }

        create(width, height, depth, withMipLevels)
    }

    override fun setSize(width: Int, height: Int, depth: Int, withMipLevels: Boolean) {
        device.checkError(width > 0) { "Texture width must be > 0" }
        device.checkError(height > 0) { "Texture height must be > 0" }
        device.checkError(depth > 0) { "Texture depth must be > 0" }

        if (width <= 0 || height <= 0 || depth <= 0)
            return

        if (handle == 0)
            create(width, height, depth, withMipLevels)
        else if (width != widthInternal || height != heightInternal || depth != depthInternal || (withMipLevels && mipLevels == 1))
            recreate(width, height, depth, withMipLevels)
    }

    override fun setImage(data: Memory, dataFormat: GPUTextureFormat, offset: Int, level: Int) {
        device.checkFail(handle != 0) { "Texture must be created first using setSize()" }
        device.checkError(level >= 0) { "Mip level must be >= 0" }
        device.checkError(offset >= 0) { "Offset must be >= 0" }

        val mipWidth = TextureUtils.mipSize(widthInternal, level)
        val mipHeight = TextureUtils.mipSize(heightInternal, level)
        val mipDepth = TextureUtils.mipSize(depthInternal, level)

        device.checkError(level < mipLevels) { "Invalid mip level: $level" }

        if (level < mipLevels)
            nglTextureSubImage3D(handle, level, offset, 0, 0, mipWidth, mipHeight, mipDepth, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
    }

    override fun compareTo(other: GPUTexture): Int {
        if (this === other) return 0
        if (other !is GLTexture3D) return 1
        return handle.compareTo(other.handle)
    }

    override fun dispose() {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
            handle = 0
            widthInternal = 0
            heightInternal = 0
            depthInternal = 0
            mipLevels = 0
            DesktopStatistics.numTextures--
        }
    }
}