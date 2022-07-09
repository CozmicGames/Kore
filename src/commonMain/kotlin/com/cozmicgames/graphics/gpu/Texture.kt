package com.cozmicgames.graphics.gpu

import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.Direction


sealed class Texture(val format: Format) : Comparable<Texture>, Disposable {
    enum class Filter {
        NEAREST,
        LINEAR
    }

    enum class Wrap {
        CLAMP,
        REPEAT,
        MIRROR
    }

    enum class Format(val isImageFormat: Boolean, val size: Int) {
        R8_UNSIGNED(true, Memory.SIZEOF_BYTE),
        R8_SIGNED(true, Memory.SIZEOF_BYTE),
        R8_UNORM(true, Memory.SIZEOF_BYTE),
        R8_SNORM(true, Memory.SIZEOF_BYTE),

        R16_UNSIGNED(true, 2 * Memory.SIZEOF_BYTE),
        R16_SIGNED(true, 2 * Memory.SIZEOF_BYTE),
        R16_FLOAT(true, 2 * Memory.SIZEOF_BYTE),
        R16_UNORM(true, 2 * Memory.SIZEOF_BYTE),
        R16_SNORM(true, 2 * Memory.SIZEOF_BYTE),

        R32_UNSIGNED(true, 4 * Memory.SIZEOF_BYTE),
        R32_SIGNED(true, 4 * Memory.SIZEOF_BYTE),
        R32_FLOAT(true, 4 * Memory.SIZEOF_BYTE),

        RG8_UNSIGNED(true, 2 * Memory.SIZEOF_BYTE),
        RG8_SIGNED(true, 2 * Memory.SIZEOF_BYTE),
        RG8_UNORM(true, 2 * Memory.SIZEOF_BYTE),
        RG8_SNORM(true, 2 * Memory.SIZEOF_BYTE),

        RG16_UNSIGNED(true, 2 * 2 * Memory.SIZEOF_BYTE),
        RG16_SIGNED(true, 2 * 2 * Memory.SIZEOF_BYTE),
        RG16_FLOAT(true, 2 * 2 * Memory.SIZEOF_BYTE),
        RG16_UNORM(true, 2 * 2 * Memory.SIZEOF_BYTE),
        RG16_SNORM(true, 2 * 2 * Memory.SIZEOF_BYTE),

        RG32_UNSIGNED(true, 2 * 4 * Memory.SIZEOF_BYTE),
        RG32_SIGNED(true, 2 * 4 * Memory.SIZEOF_BYTE),
        RG32_FLOAT(true, 2 * 4 * Memory.SIZEOF_BYTE),

        RGBA8_UNSIGNED(true, 4 * Memory.SIZEOF_BYTE),
        RGBA8_SIGNED(true, 4 * Memory.SIZEOF_BYTE),
        RGBA8_UNORM(true, 4 * Memory.SIZEOF_BYTE),
        RGBA8_SNORM(true, 4 * Memory.SIZEOF_BYTE),

        RGBA16_UNSIGNED(true, 4 * 2 * Memory.SIZEOF_BYTE),
        RGBA16_SIGNED(true, 4 * 2 * Memory.SIZEOF_BYTE),
        RGBA16_FLOAT(true, 4 * 2 * Memory.SIZEOF_BYTE),
        RGBA16_UNORM(true, 4 * 2 * Memory.SIZEOF_BYTE),
        RGBA16_SNORM(true, 4 * 2 * Memory.SIZEOF_BYTE),

        RGBA32_UNSIGNED(true, 4 * 4 * Memory.SIZEOF_BYTE),
        RGBA32_SIGNED(true, 4 * 4 * Memory.SIZEOF_BYTE),
        RGBA32_FLOAT(true, 4 * 4 * Memory.SIZEOF_BYTE),

        DEPTH16(false, 0),
        DEPTH24(false, 0),
        DEPTH24STENCIL8(false, 0),
        DEPTH32(false, 0),
        DEPTH32F(false, 0),
        STENCIL8(false, 0)
    }

    abstract fun setSampler(sampler: Sampler)
}

abstract class Texture2D(format: Format) : Texture(format) {
    abstract val width: Int
    abstract val height: Int

    abstract fun setSize(width: Int, height: Int)
    abstract fun setImage(width: Int, height: Int, data: Memory, dataFormat: Format = Format.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
    abstract fun setSubImage(x: Int, y: Int, width: Int, height: Int, data: Memory, dataFormat: Format = Format.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
    abstract fun getImage(data: Memory, offset: Int = 0, format: Format = this.format, level: Int = 0)
}

val Texture2D.size get() = width * height * format.size

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

abstract class TextureCube(format: Format) : Texture(format) {
    abstract fun setImages(width: Int, height: Int, format: Format = Format.RGBA8_UNORM, block: CubemapSides.() -> Unit)
    abstract fun setDirectionImage(direction: Direction, width: Int, height: Int, data: Memory, format: Format = Format.RGBA8_UNORM, offset: Int = 0)
}

abstract class Texture3D(format: Format) : Texture(format) {
    abstract val width: Int
    abstract val height: Int
    abstract val depth: Int

    abstract fun setSize(width: Int, height: Int, depth: Int)
    abstract fun setImage(width: Int, height: Int, depth: Int, data: Memory, dataFormat: Format = Format.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
    abstract fun setSubImage(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, data: Memory, dataFormat: Format = Format.RGBA8_UNORM, offset: Int = 0, level: Int = 0)
}
