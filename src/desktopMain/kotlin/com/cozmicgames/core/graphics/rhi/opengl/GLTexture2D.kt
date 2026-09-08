package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.TextureUtils
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.rhi.GPUTexture2D
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.internal.checkError
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL46C.*

class GLTexture2D(private val device: GLDevice, format: GPUTextureFormat) : GPUTexture2D(format), GLTexture {
    override var handle = 0
    private var widthInternal = 0
    private var heightInternal = 0
    private var numArrayLayersInternal = 0
    private var mipLevels = 0

    override val width: Int get() = widthInternal
    override val height: Int get() = heightInternal
    override val numArrayLayers: Int get() = numArrayLayersInternal

    private fun calculateMemorySize(): Long = TextureUtils.memorySize(widthInternal, heightInternal, numArrayLayersInternal, format, mipLevels)

    private fun create(width: Int, height: Int, numArrayLayers: Int, withMipLevels: Boolean) {
        mipLevels = if (withMipLevels) TextureUtils.mipLevels(width, height) else 1
        handle = glCreateTextures(if (numArrayLayers > 1) GL_TEXTURE_2D_ARRAY else GL_TEXTURE_2D)

        if (numArrayLayers > 1)
            glTextureStorage3D(handle, mipLevels, format.toGLFormat(), width, height, numArrayLayers)
        else
            glTextureStorage2D(handle, mipLevels, format.toGLFormat(), width, height)

        widthInternal = width
        heightInternal = height
        numArrayLayersInternal = numArrayLayers
        device.usedMemoryInternal += calculateMemorySize()
        DesktopStatistics.numTextures++
    }

    private fun recreate(width: Int, height: Int, numArrayLayers: Int, withMipLevels: Boolean) {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
        }

        create(width, height, numArrayLayers, withMipLevels)
    }

    override fun setSize(width: Int, height: Int, numArrayLayers: Int, withMipLevels: Boolean) {
        device.checkError(width > 0) { "Texture width must be > 0" }
        device.checkError(height > 0) { "Texture height must be > 0" }
        device.checkError(numArrayLayers > 0) { "Number of array layers must be > 0" }

        if (width <= 0 || height <= 0 || numArrayLayers <= 0)
            return

        if (handle == 0)
            create(width, height, numArrayLayers, withMipLevels)
        else if (width != widthInternal || height != heightInternal || numArrayLayers != numArrayLayersInternal || (withMipLevels && mipLevels == 1))
            recreate(width, height, numArrayLayers, withMipLevels)
    }

    override fun setImage(data: Memory, dataFormat: GPUTextureFormat, offset: Int, level: Int, layer: Int) {
        device.checkFail(handle != 0) { "Texture must be created first using setSize()" }
        device.checkError(level >= 0) { "Mip level must be >= 0" }
        device.checkError(offset >= 0) { "Offset must be >= 0" }
        device.checkError(layer >= 0) { "Layer must be >= 0" }

        val mipWidth = TextureUtils.mipSize(widthInternal, level)
        val mipHeight = TextureUtils.mipSize(heightInternal, level)

        device.checkError(level < mipLevels) { "Invalid mip level: $level" }
        device.checkError(layer < numArrayLayersInternal) { "Invalid array layer: $layer" }

        if (level >= mipLevels || layer >= numArrayLayersInternal)
            return

        if (numArrayLayersInternal > 1)
            nglTextureSubImage3D(handle, level, offset, 0, layer, mipWidth, mipHeight, 1, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
        else
            nglTextureSubImage2D(handle, level, offset, 0, mipWidth, mipHeight, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
    }

    override fun compareTo(other: GPUTexture): Int {
        if (this === other) return 0
        if (other !is GLTexture2D) return 1
        return handle.compareTo(other.handle)
    }

    override fun dispose() {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
            handle = 0
            widthInternal = 0
            heightInternal = 0
            numArrayLayersInternal = 0
            mipLevels = 0
            DesktopStatistics.numTextures--
        }
    }
}