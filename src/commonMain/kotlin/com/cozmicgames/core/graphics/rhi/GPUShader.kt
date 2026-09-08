package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.utils.Disposable

interface GPUShader: Disposable {
    enum class ResourceType {
        BUFFER,
        TEXTURE,
        IMAGE
    }

    class Resource(val name: String, val type: ResourceType, val binding: Int)

    val resources: List<Resource>
}