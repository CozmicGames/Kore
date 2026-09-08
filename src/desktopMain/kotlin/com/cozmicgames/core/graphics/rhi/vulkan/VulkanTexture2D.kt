package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.rhi.GPUTexture2D
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.memory.Memory

class VulkanTexture2D(format: GPUTextureFormat): GPUTexture2D(format) {
    var knownLayout: Int? = null

    val view: Long
        get() {
            TODO()
        }
    val texture: Long
        get() {
            TODO()
        }

    override val width: Int
        get() = TODO("Not yet implemented")
    override val height: Int
        get() = TODO("Not yet implemented")
    override val numArrayLayers: Int
        get() = TODO("Not yet implemented")

    override fun setSize(width: Int, height: Int, numArrayLayers: Int, withMipLevels: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setImage(data: Memory, dataFormat: GPUTextureFormat, offset: Int, level: Int, layer: Int) {
        TODO("Not yet implemented")
    }


    override fun dispose() {

    }

    override fun compareTo(other: GPUTexture): Int {
        TODO("Not yet implemented")
    }
}