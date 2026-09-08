package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUTextureFormat

internal interface GLTexture {
    val handle: Int
    val format: GPUTextureFormat
}