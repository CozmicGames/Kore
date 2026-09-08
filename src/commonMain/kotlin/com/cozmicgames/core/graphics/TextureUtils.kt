package com.cozmicgames.core.graphics

import com.cozmicgames.core.graphics.rhi.GPUTextureFormat

object TextureUtils {
    fun mipSize(size: Int, level: Int): Int = maxOf(1, size shr level)

    fun mipLevels(width: Int, height: Int = 1, depth: Int = 1): Int = 32 - maxOf(width, height, depth).countLeadingZeroBits()

    fun mipMemorySize(width: Int, height: Int, depth: Int, format: GPUTextureFormat, level: Int): Long = mipSize(width, level).toLong() * mipSize(height, level) * mipSize(depth, level) * format.bytesPerPixel

    fun memorySize(width: Int, height: Int, depth: Int, format: GPUTextureFormat, mipLevels: Int): Long {
        var size = 0L
        for (level in 0 until mipLevels)
            size += mipMemorySize(width, height, depth, format, level)
        return size
    }
}