package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.PlatformType

interface GPUShaderSource {
    enum class Type {
        GRAPHICS,
        COMPUTE
    }

    val platformType: PlatformType
    val type: Type
}