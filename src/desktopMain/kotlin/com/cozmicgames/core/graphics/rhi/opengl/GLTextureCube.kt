package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.TextureUtils
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.rhi.GPUTextureCube
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.internal.checkError
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.memory.Memory
import org.lwjgl.opengl.GL46C.*

class GLTextureCube(private val device: GLDevice, format: GPUTextureFormat) : GPUTextureCube(format), GLTexture {
    override var handle = 0
    private var widthInternal = 0
    private var heightInternal = 0
    private var numArrayLayersInternal = 0
    private var mipLevels = 0

    private fun calculateMemorySize(): Long = TextureUtils.memorySize(widthInternal, heightInternal, numArrayLayersInternal * 6, format, mipLevels)

    private fun create(width: Int, height: Int, layers: Int, withMipLevels: Boolean) {
        mipLevels = if (withMipLevels) TextureUtils.mipLevels(width, height) else 1
        handle = glCreateTextures(if (layers > 1) GL_TEXTURE_CUBE_MAP_ARRAY else GL_TEXTURE_CUBE_MAP)

        if (layers > 1)
            glTextureStorage3D(handle, mipLevels, format.toGLFormat(), width, height, layers * 6)
        else
            glTextureStorage2D(handle, mipLevels, format.toGLFormat(), width, height)

        widthInternal = width
        heightInternal = height
        numArrayLayersInternal = layers
        device.usedMemoryInternal += calculateMemorySize()
        DesktopStatistics.numTextures++
    }

    private fun recreate(width: Int, height: Int, layers: Int, withMipLevels: Boolean) {
        if (handle != 0) {
            device.usedMemoryInternal -= calculateMemorySize()
            glDeleteTextures(handle)
        }

        create(width, height, layers, withMipLevels)
    }

    override fun setSize(width: Int, height: Int, numArrayLayers: Int, withMipLevels: Boolean) {
        device.checkError(width > 0) { "Texture width must be > 0" }
        device.checkError(height > 0) { "Texture height must be > 0" }
        device.checkError(width == height) { "Cubemap width and height must be equal" }
        device.checkError(numArrayLayers > 0) { "Number of array layers must be > 0" }

        if (width <= 0 || height <= 0 || width != height || numArrayLayers <= 0)
            return

        if (handle == 0)
            create(width, height, numArrayLayers, withMipLevels)
        else if (width != widthInternal || height != heightInternal || numArrayLayers != numArrayLayersInternal || (withMipLevels && mipLevels == 1))
            recreate(width, height, numArrayLayers, withMipLevels)
    }

    override fun setImages(format: GPUTextureFormat, level: Int, layer: Int, block: CubemapSides.() -> Unit) {
        device.checkFail(handle != 0) { "Texture must be created first using setSize()" }

        if (handle == 0)
            return

        device.checkError(level >= 0 && level < mipLevels) { "Invalid mip level: $level" }
        device.checkError(layer >= 0 && layer < numArrayLayersInternal) { "Invalid cubemap layer: $layer" }

        if (level < 0 || level >= mipLevels || layer < 0 || layer >= numArrayLayersInternal)
            return

        val sides = CubemapSides().apply(block)
        val mipWidth = TextureUtils.mipSize(widthInternal, level)
        val mipHeight = TextureUtils.mipSize(heightInternal, level)
        val zOffset = layer * 6

        fun upload(face: Int, data: Memory?, offset: Int) {
            if (data == null)
                return

            device.checkError(offset >= 0) { "Offset must be >= 0" }

            if (offset >= 0)
                nglTextureSubImage3D(handle, level, 0, 0, zOffset + face, mipWidth, mipHeight, 1, format.toGLFormat(), format.toGLType(), data.address + offset)
        }

        upload(0, sides.positiveX, sides.offsetPositiveX)
        upload(1, sides.negativeX, sides.offsetNegativeX)
        upload(2, sides.positiveY, sides.offsetPositiveY)
        upload(3, sides.negativeY, sides.offsetNegativeY)
        upload(4, sides.positiveZ, sides.offsetPositiveZ)
        upload(5, sides.negativeZ, sides.offsetNegativeZ)
    }

    override fun compareTo(other: GPUTexture): Int {
        if (this === other) return 0
        if (other !is GLTextureCube) return 1
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