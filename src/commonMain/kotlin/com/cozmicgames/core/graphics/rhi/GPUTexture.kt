package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.memory.Memory
import com.cozmicgames.core.utils.Disposable


sealed interface GPUTexture : Comparable<GPUTexture>, Disposable {
    enum class Type {
        TEXTURE_1D,
        TEXTURE_2D,
        TEXTURE_3D,
        TEXTURE_CUBE
    }

    val type: Type
    val format: GPUTextureFormat
}

abstract class GPUTexture1D(override val format: GPUTextureFormat) : GPUTexture {
    final override val type = GPUTexture.Type.TEXTURE_1D

    abstract val size: Int

    abstract fun setSize(size: Int, withMipLevels: Boolean)
    abstract fun setImage(data: Memory, dataFormat: GPUTextureFormat = GPUTextureFormat.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
}

abstract class GPUTexture2D(override val format: GPUTextureFormat) : GPUTexture {
    final override val type = GPUTexture.Type.TEXTURE_2D

    abstract val width: Int
    abstract val height: Int
    abstract val numArrayLayers: Int

    abstract fun setSize(width: Int, height: Int, numArrayLayers: Int = 1, withMipLevels: Boolean)
    abstract fun setImage(data: Memory, dataFormat: GPUTextureFormat = GPUTextureFormat.RGBA8_UNORM, offset: Int = 0, level: Int = 0, layer: Int = 0)
}

abstract class GPUTexture3D(override val format: GPUTextureFormat) : GPUTexture {
    final override val type = GPUTexture.Type.TEXTURE_3D

    abstract val width: Int
    abstract val height: Int
    abstract val depth: Int

    abstract fun setSize(width: Int, height: Int, depth: Int, withMipLevels: Boolean)
    abstract fun setImage(data: Memory, dataFormat: GPUTextureFormat = GPUTextureFormat.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
}

abstract class GPUTextureCube(override val format: GPUTextureFormat) : GPUTexture {
    final override val type = GPUTexture.Type.TEXTURE_CUBE

    class CubemapSides {
        var negativeX: Memory? = null
        var offsetNegativeX = 0
        var positiveX: Memory? = null
        var offsetPositiveX = 0
        var negativeY: Memory? = null
        var offsetNegativeY = 0
        var positiveY: Memory? = null
        var offsetPositiveY = 0
        var negativeZ: Memory? = null
        var offsetNegativeZ = 0
        var positiveZ: Memory? = null
        var offsetPositiveZ = 0
    }

    abstract fun setSize(width: Int, height: Int, numArrayLayers: Int = 1, withMipLevels: Boolean)
    abstract fun setImages(format: GPUTextureFormat = GPUTextureFormat.RGBA8_UNORM, level: Int = 0, layer: Int = 0, block : CubemapSides .() -> Unit)
}

