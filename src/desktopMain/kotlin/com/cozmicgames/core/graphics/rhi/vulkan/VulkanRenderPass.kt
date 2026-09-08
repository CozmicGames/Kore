package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.graphics.rhi.GPURenderPass
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkRenderingAttachmentInfo
import org.lwjgl.vulkan.VkRenderingInfo
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkDependencyInfo
import org.lwjgl.vulkan.VkImageMemoryBarrier2

class VulkanRenderPass(private val builder: VulkanRenderPassBuilder) : GPURenderPass {
    private class RenderTexture(val texture: VulkanTexture2D, val type: Type) {
        enum class Type {
            COLOR,
            DEPTH,
            STENCIL
        }

        val hasOptimalLayout
            get() = when (type) {
                Type.COLOR -> texture.knownLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                Type.DEPTH -> texture.knownLayout == VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL
                Type.STENCIL -> texture.knownLayout == VK_IMAGE_LAYOUT_STENCIL_ATTACHMENT_OPTIMAL
            }
    }

    val pRenderingInfo: VkRenderingInfo

    private val pColorAttachments: VkRenderingAttachmentInfo.Buffer?
    private val pDepthAttachment: VkRenderingAttachmentInfo?
    private val pStencilAttachment: VkRenderingAttachmentInfo?
    private val renderTextures = arrayListOf<RenderTexture>()

    init {
        pColorAttachments = if (builder.colorAttachments.isNotEmpty()) VkRenderingAttachmentInfo.calloc(builder.colorAttachments.size) else null
        pDepthAttachment = if (builder.depthAttachment != null) VkRenderingAttachmentInfo.calloc() else null
        pStencilAttachment = if (builder.stencilAttachment != null) VkRenderingAttachmentInfo.calloc() else null
        pRenderingInfo = VkRenderingInfo.calloc()

        if (pColorAttachments != null) {
            builder.colorAttachments.forEachIndexed { index, attachment ->
                val pColorAttachment = pColorAttachments.get(index)
                pColorAttachment.`sType$Default`()
                val clearColor = attachment.clearColor
                if (clearColor != null)
                    pColorAttachment.clearValue {
                        it.color {
                            it.float32(0, clearColor.r)
                            it.float32(1, clearColor.g)
                            it.float32(2, clearColor.b)
                            it.float32(3, clearColor.a)
                        }
                    }
                pColorAttachment.loadOp(
                    when (attachment.loadOp) {
                        GPURenderPass.LoadOp.CLEAR -> VK_ATTACHMENT_LOAD_OP_CLEAR
                        GPURenderPass.LoadOp.LOAD -> VK_ATTACHMENT_LOAD_OP_LOAD
                        GPURenderPass.LoadOp.DONT_CARE -> VK_ATTACHMENT_LOAD_OP_DONT_CARE
                    }
                )
                pColorAttachment.storeOp(
                    when (attachment.storeOp) {
                        GPURenderPass.StoreOp.DONT_CARE -> VK_ATTACHMENT_STORE_OP_DONT_CARE
                        GPURenderPass.StoreOp.STORE -> VK_ATTACHMENT_STORE_OP_STORE
                    }
                )
                pColorAttachment.imageView(attachment.texture.view)
                pColorAttachment.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            }
        }

        val depthAttachment = builder.depthAttachment
        if (pDepthAttachment != null && depthAttachment != null) {
            pDepthAttachment.`sType$Default`()
            val clearDepth = depthAttachment.clearDepth
            if (clearDepth != null)
                pDepthAttachment.clearValue {
                    it.depthStencil {
                        it.depth(clearDepth)
                    }
                }
            pDepthAttachment.loadOp(
                when (depthAttachment.loadOp) {
                    GPURenderPass.LoadOp.CLEAR -> VK_ATTACHMENT_LOAD_OP_CLEAR
                    GPURenderPass.LoadOp.LOAD -> VK_ATTACHMENT_LOAD_OP_LOAD
                    GPURenderPass.LoadOp.DONT_CARE -> VK_ATTACHMENT_LOAD_OP_DONT_CARE
                }
            )
            pDepthAttachment.storeOp(
                when (depthAttachment.storeOp) {
                    GPURenderPass.StoreOp.DONT_CARE -> VK_ATTACHMENT_STORE_OP_DONT_CARE
                    GPURenderPass.StoreOp.STORE -> VK_ATTACHMENT_STORE_OP_STORE
                }
            )
            pDepthAttachment.imageView(depthAttachment.texture.view)
            pDepthAttachment.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        }

        val stencilAttachment = builder.stencilAttachment
        if (pStencilAttachment != null && stencilAttachment != null) {
            pStencilAttachment.`sType$Default`()
            val clearStencil = stencilAttachment.clearStencil
            if (clearStencil != null)
                pStencilAttachment.clearValue {
                    it.depthStencil {
                        it.stencil(clearStencil)
                    }
                }
            pStencilAttachment.loadOp(
                when (stencilAttachment.loadOp) {
                    GPURenderPass.LoadOp.CLEAR -> VK_ATTACHMENT_LOAD_OP_CLEAR
                    GPURenderPass.LoadOp.LOAD -> VK_ATTACHMENT_LOAD_OP_LOAD
                    GPURenderPass.LoadOp.DONT_CARE -> VK_ATTACHMENT_LOAD_OP_DONT_CARE
                }
            )
            pStencilAttachment.storeOp(
                when (stencilAttachment.storeOp) {
                    GPURenderPass.StoreOp.DONT_CARE -> VK_ATTACHMENT_STORE_OP_DONT_CARE
                    GPURenderPass.StoreOp.STORE -> VK_ATTACHMENT_STORE_OP_STORE
                }
            )
            pStencilAttachment.imageView(stencilAttachment.texture.view)
            pStencilAttachment.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        }

        pRenderingInfo.`sType$Default`()
        pRenderingInfo.layerCount(1)
        if (pColorAttachments != null)
            pRenderingInfo.pColorAttachments(pColorAttachments)
        if (pDepthAttachment != null)
            pRenderingInfo.pDepthAttachment(pDepthAttachment)
        if (pStencilAttachment != null)
            pRenderingInfo.pStencilAttachment(pStencilAttachment)
        pRenderingInfo.renderArea {
            it.offset {
                it.x(0)
                it.y(0)
            }
            it.extent {
                it.width(builder.width)
                it.height(builder.height)
            }
        }

        builder.colorAttachments.forEach {
            renderTextures += RenderTexture(it.texture, RenderTexture.Type.COLOR)
        }

        if (depthAttachment != null)
            renderTextures += RenderTexture(depthAttachment.texture, RenderTexture.Type.DEPTH)

        if (stencilAttachment != null)
            renderTextures += RenderTexture(stencilAttachment.texture, RenderTexture.Type.STENCIL)
    }

    fun ensureLayout(commandBuffer: VkCommandBuffer) {
        stackPush().use { stack ->
            val barrierCount = renderTextures.count { !it.hasOptimalLayout }
            val pLayoutBarriers = VkImageMemoryBarrier2.calloc(barrierCount, stack)
            var barrierIndex = 0

            renderTextures.forEach {
                val pLayoutBarrier = pLayoutBarriers.get(barrierIndex)
                pLayoutBarrier.`sType$Default`()
                pLayoutBarrier.image(it.texture.texture)
                pLayoutBarrier.oldLayout(it.texture.knownLayout ?: VK_IMAGE_LAYOUT_UNDEFINED)
                pLayoutBarrier.subresourceRange {
                    it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    it.baseMipLevel(0)
                    it.levelCount(1)
                    it.baseArrayLayer(0)
                    it.layerCount(1)
                }

                when (it.type) {
                    RenderTexture.Type.COLOR -> {
                        pLayoutBarrier.srcStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                        pLayoutBarrier.srcAccessMask(VK_ACCESS_2_NONE)
                        pLayoutBarrier.dstStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                        pLayoutBarrier.dstAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
                        pLayoutBarrier.newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                        it.texture.knownLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                    }

                    RenderTexture.Type.DEPTH -> {
                        pLayoutBarrier.srcStageMask(VK_PIPELINE_STAGE_2_NONE)
                        pLayoutBarrier.srcAccessMask(VK_ACCESS_2_NONE)
                        pLayoutBarrier.dstStageMask(VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT or VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT)
                        pLayoutBarrier.dstAccessMask(VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
                        pLayoutBarrier.newLayout(VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
                        it.texture.knownLayout = VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL
                    }

                    RenderTexture.Type.STENCIL -> {
                        pLayoutBarrier.srcStageMask(VK_PIPELINE_STAGE_2_NONE)
                        pLayoutBarrier.srcAccessMask(VK_ACCESS_2_NONE)
                        pLayoutBarrier.dstStageMask(VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT or VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT)
                        pLayoutBarrier.dstAccessMask(VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
                        pLayoutBarrier.newLayout(VK_IMAGE_LAYOUT_STENCIL_ATTACHMENT_OPTIMAL)
                        it.texture.knownLayout = VK_IMAGE_LAYOUT_STENCIL_ATTACHMENT_OPTIMAL
                    }
                }

                barrierIndex++
            }

            val pDependencyInfo = VkDependencyInfo.calloc(stack)
            pDependencyInfo.`sType$Default`()
            pDependencyInfo.pImageMemoryBarriers(pLayoutBarriers)

            vkCmdPipelineBarrier2(commandBuffer, pDependencyInfo)
        }
    }

    override fun dispose() {
        pRenderingInfo.free()
        pColorAttachments?.free()
        pDepthAttachment?.free()
        pStencilAttachment?.free()
    }
}