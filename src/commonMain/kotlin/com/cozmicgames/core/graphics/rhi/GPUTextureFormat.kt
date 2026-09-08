package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.memory.Memory

enum class GPUTextureFormat(val isImageFormat: Boolean, val bytesPerPixel: Int, val isDepthFormat: Boolean = false, val isStencilFormat: Boolean = false) {
    R8_UNSIGNED(true, Memory.Companion.SIZEOF_BYTE),
    R8_SIGNED(true, Memory.Companion.SIZEOF_BYTE),
    R8_UNORM(true, Memory.Companion.SIZEOF_BYTE),
    R8_SNORM(true, Memory.Companion.SIZEOF_BYTE),

    R16_UNSIGNED(true, 2 * Memory.Companion.SIZEOF_BYTE),
    R16_SIGNED(true, 2 * Memory.Companion.SIZEOF_BYTE),
    R16_FLOAT(true, 2 * Memory.Companion.SIZEOF_BYTE),
    R16_UNORM(true, 2 * Memory.Companion.SIZEOF_BYTE),
    R16_SNORM(true, 2 * Memory.Companion.SIZEOF_BYTE),

    R32_UNSIGNED(true, 4 * Memory.Companion.SIZEOF_BYTE),
    R32_SIGNED(true, 4 * Memory.Companion.SIZEOF_BYTE),
    R32_FLOAT(true, 4 * Memory.Companion.SIZEOF_BYTE),

    RG8_UNSIGNED(true, 2 * Memory.Companion.SIZEOF_BYTE),
    RG8_SIGNED(true, 2 * Memory.Companion.SIZEOF_BYTE),
    RG8_UNORM(true, 2 * Memory.Companion.SIZEOF_BYTE),
    RG8_SNORM(true, 2 * Memory.Companion.SIZEOF_BYTE),

    RG16_UNSIGNED(true, 2 * 2 * Memory.Companion.SIZEOF_BYTE),
    RG16_SIGNED(true, 2 * 2 * Memory.Companion.SIZEOF_BYTE),
    RG16_FLOAT(true, 2 * 2 * Memory.Companion.SIZEOF_BYTE),
    RG16_UNORM(true, 2 * 2 * Memory.Companion.SIZEOF_BYTE),
    RG16_SNORM(true, 2 * 2 * Memory.Companion.SIZEOF_BYTE),

    RG32_UNSIGNED(true, 2 * 4 * Memory.Companion.SIZEOF_BYTE),
    RG32_SIGNED(true, 2 * 4 * Memory.Companion.SIZEOF_BYTE),
    RG32_FLOAT(true, 2 * 4 * Memory.Companion.SIZEOF_BYTE),

    RGBA8_UNSIGNED(true, 4 * Memory.Companion.SIZEOF_BYTE),
    RGBA8_SIGNED(true, 4 * Memory.Companion.SIZEOF_BYTE),
    RGBA8_UNORM(true, 4 * Memory.Companion.SIZEOF_BYTE),
    RGBA8_SNORM(true, 4 * Memory.Companion.SIZEOF_BYTE),

    RGBA16_UNSIGNED(true, 4 * 2 * Memory.Companion.SIZEOF_BYTE),
    RGBA16_SIGNED(true, 4 * 2 * Memory.Companion.SIZEOF_BYTE),
    RGBA16_FLOAT(true, 4 * 2 * Memory.Companion.SIZEOF_BYTE),
    RGBA16_UNORM(true, 4 * 2 * Memory.Companion.SIZEOF_BYTE),
    RGBA16_SNORM(true, 4 * 2 * Memory.Companion.SIZEOF_BYTE),

    RGBA32_UNSIGNED(true, 4 * 4 * Memory.Companion.SIZEOF_BYTE),
    RGBA32_SIGNED(true, 4 * 4 * Memory.Companion.SIZEOF_BYTE),
    RGBA32_FLOAT(true, 4 * 4 * Memory.Companion.SIZEOF_BYTE),

    DEPTH16(false, 0, true),
    DEPTH24(false, 0, true),
    DEPTH24STENCIL8(false, 0, true, true),
    DEPTH32(false, 0, true),
    DEPTH32F(false, 0, true),
    STENCIL8(false, 0, isStencilFormat = true)
}