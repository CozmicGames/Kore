package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.graphics.rhi.GPURenderPass
import com.cozmicgames.core.graphics.rhi.GPUTexture2D
import com.cozmicgames.core.utils.Color

class VulkanRenderPassBuilder : GPURenderPass.Builder {
    class ColorAttachment(val texture: VulkanTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearColor: Color?)
    class DepthAttachment(val texture: VulkanTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearDepth: Float?)
    class StencilAttachment(val texture: VulkanTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearStencil: Int?)

    val usedTextures get() = usedTexturesInternal as List<VulkanTexture2D>

    val width get() = widthInternal
    val height get() = heightInternal

    val colorAttachments get() = colorAttachmentsInternal as List<ColorAttachment>
    val depthAttachment get() = depthAttachmentInternal
    val stencilAttachment get() = stencilAttachmentInternal

    private val usedTexturesInternal = arrayListOf<VulkanTexture2D>()

    private var widthInternal = 0
    private var heightInternal = 0

    private var colorAttachmentsInternal = arrayListOf<ColorAttachment>()
    private var depthAttachmentInternal: DepthAttachment? = null
    private var stencilAttachmentInternal: StencilAttachment? = null

    override fun addColorAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearColor: Color) {
        colorAttachmentsInternal += ColorAttachment(texture as VulkanTexture2D, level, layer, loadOp, storeOp, clearColor)
    }

    override fun setDepthAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearDepth: Float) {
        depthAttachmentInternal = DepthAttachment(texture as VulkanTexture2D, level, layer, loadOp, storeOp, clearDepth)
    }

    override fun setStencilAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearStencil: Int) {
        stencilAttachmentInternal = StencilAttachment(texture as VulkanTexture2D, level, layer, loadOp, storeOp, clearStencil)
    }

    //TODO:
    fun build(): VulkanRenderPass {
        usedTexturesInternal.clear()

        colorAttachments.forEach {
            usedTexturesInternal += it.texture
        }

        depthAttachment?.let {
            usedTexturesInternal += it.texture
        }

        stencilAttachment?.let {
            usedTexturesInternal += it.texture
        }

        if (usedTexturesInternal.isEmpty())
            throw AssertionError("RenderPass can't be empty")

        val width = usedTexturesInternal.first().width
        val height = usedTexturesInternal.first().height

        usedTexturesInternal.forEach {
            if (it.width != width || it.height != height)
                throw AssertionError("RenderPass textures must be the same dimensions")
        }

        widthInternal = width
        heightInternal = height

        return VulkanRenderPass(this)
    }
}