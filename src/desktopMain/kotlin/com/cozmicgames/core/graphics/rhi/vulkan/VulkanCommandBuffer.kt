package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.graphics.rhi.GPUCommandBuffer
import com.cozmicgames.core.graphics.rhi.GPUBuffer
import com.cozmicgames.core.graphics.rhi.GPUImageAccess
import com.cozmicgames.core.graphics.rhi.GPUPipeline
import com.cozmicgames.core.graphics.rhi.GPURenderPass
import com.cozmicgames.core.graphics.rhi.GPUSampler
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.Primitive
import com.cozmicgames.core.utils.Color
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
import org.lwjgl.vulkan.VkDependencyInfo
import org.lwjgl.vulkan.VkImageMemoryBarrier2
import org.lwjgl.vulkan.VkRect2D
import org.lwjgl.vulkan.VkRenderingAttachmentInfo
import org.lwjgl.vulkan.VkRenderingInfo
import org.lwjgl.vulkan.VkViewport

class VulkanCommandBuffer(val vulkanDevice: VulkanDevice, val vulkanCommandBufferObject: VkCommandBuffer) : GPUCommandBuffer {
    private fun getMainImage() = vulkanDevice.swapchain.images[vulkanDevice.currentImageIndex]

    private fun getMainImageView() = vulkanDevice.swapchain.imageViews[vulkanDevice.currentImageIndex]

    override fun beginMainRenderPass(clearColor: Color, clearDepth: Float) {
        val mainImage = getMainImage()
        val mainImageView = getMainImageView()

        stackPush().use { stack ->
            val pLayoutBarrier = VkImageMemoryBarrier2.calloc(1, stack)
            pLayoutBarrier.`sType$Default`()
            pLayoutBarrier.srcStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
            pLayoutBarrier.srcAccessMask(0)
            pLayoutBarrier.dstStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
            pLayoutBarrier.dstAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
            pLayoutBarrier.oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            pLayoutBarrier.newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            pLayoutBarrier.image(mainImage)
            pLayoutBarrier.subresourceRange {
                it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                it.baseMipLevel(0)
                it.levelCount(1)
                it.baseArrayLayer(0)
                it.layerCount(1)
            }

            val pDependencyInfo = VkDependencyInfo.calloc(stack)
            pDependencyInfo.`sType$Default`()
            pDependencyInfo.pImageMemoryBarriers(pLayoutBarrier)

            vkCmdPipelineBarrier2(vulkanCommandBufferObject, pDependencyInfo)

            val pColorAttachments = VkRenderingAttachmentInfo.calloc(1, stack)
            pColorAttachments.`sType$Default`()
            pColorAttachments.clearValue {
                it.color {
                    it.float32(0, clearColor.r)
                    it.float32(1, clearColor.g)
                    it.float32(2, clearColor.b)
                    it.float32(3, clearColor.a)
                }
                it.depthStencil {
                    it.depth(clearDepth)
                }
            }
            pColorAttachments.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
            pColorAttachments.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
            pColorAttachments.imageView(mainImageView)
            pColorAttachments.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

            val pRenderingInfo = VkRenderingInfo.calloc(stack)
            pRenderingInfo.`sType$Default`()
            pRenderingInfo.layerCount(1)
            pRenderingInfo.pColorAttachments(pColorAttachments)
            pRenderingInfo.renderArea {
                it.offset {
                    it.x(0)
                    it.y(0)
                }
                it.extent {
                    it.width(vulkanDevice.swapchain.width)
                    it.height(vulkanDevice.swapchain.height)
                }
            }

            vkCmdBeginRendering(vulkanCommandBufferObject, pRenderingInfo)
        }
    }

    override fun endMainRenderPass() {
        val mainImage = getMainImage()

        stackPush().use { stack ->
            vkCmdEndRendering(vulkanCommandBufferObject)

            val pLayoutBarrier = VkImageMemoryBarrier2.calloc(1, stack)
            pLayoutBarrier.`sType$Default`()
            pLayoutBarrier.srcStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
            pLayoutBarrier.srcAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
            pLayoutBarrier.dstStageMask(VK_PIPELINE_STAGE_2_NONE)
            pLayoutBarrier.dstAccessMask(0)
            pLayoutBarrier.oldLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            pLayoutBarrier.newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
            pLayoutBarrier.image(mainImage)
            pLayoutBarrier.subresourceRange {
                it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                it.baseMipLevel(0)
                it.levelCount(1)
                it.baseArrayLayer(0)
                it.layerCount(1)
            }

            val pDependencyInfo = VkDependencyInfo.calloc(stack)
            pDependencyInfo.`sType$Default`()
            pDependencyInfo.pImageMemoryBarriers(pLayoutBarrier)

            vkCmdPipelineBarrier2(vulkanCommandBufferObject, pDependencyInfo)
        }
    }

    override fun beginRenderPass(renderPass: GPURenderPass) {
        renderPass as VulkanRenderPass

        renderPass.ensureLayout(vulkanCommandBufferObject)

        vkCmdBeginRendering(vulkanCommandBufferObject, renderPass.pRenderingInfo)
    }

    override fun endRenderPass() {
        vkCmdEndRendering(vulkanCommandBufferObject)
    }

    override fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        stackPush().use { stack ->
            val pViewport = VkViewport.calloc(1, stack)

            pViewport.x(x.toFloat())
            pViewport.y(y.toFloat())
            pViewport.width(width.toFloat())
            pViewport.height(height.toFloat())
            // pViewport.minDepth(minDepth) //TODO
            // pViewport.maxDepth(maxDepth) //TODO

            vkCmdSetViewport(vulkanCommandBufferObject, 0, pViewport)
        }
    }

    override fun setScissor(x: Int, y: Int, width: Int, height: Int) {
        stackPush().use { stack ->
            val pScissorRect = VkRect2D.calloc(1, stack)
            pScissorRect.offset {
                it.x(x)
                it.y(y)
            }
            pScissorRect.extent {
                it.width(width)
                it.height(height)
            }
            vkCmdSetScissor(vulkanCommandBufferObject, 0, pScissorRect)
        }
    }

    override fun setPipeline(pipeline: GPUPipeline) {
        TODO("Not yet implemented")
    }

    override fun setBuffer(binding: Int, buffer: GPUBuffer) {
        TODO("Not yet implemented")
    }

    override fun setTexture(binding: Int, texture: GPUTexture) {
        TODO("Not yet implemented")
    }

    override fun setSampler(binding: Int, sampler: GPUSampler) {
        TODO("Not yet implemented")
    }

    override fun setImage(binding: Int, texture: GPUTexture, level: Int, layer: Int, access: GPUImageAccess) {
        TODO("Not yet implemented")
    }

    override fun draw(primitive: Primitive, count: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        // TODO: Handle primitive type

        vkCmdDraw(vulkanCommandBufferObject, count, instanceCount, firstVertex, firstInstance)
    }

    override fun drawIndirect(primitive: Primitive, buffer: GPUBuffer, offset: Int, drawCount: Int, stride: Int) {
        TODO("Not yet implemented")
    }

    override fun dispatch(x: Int, y: Int, z: Int) {
        vkCmdDispatch(vulkanCommandBufferObject, x, y, z)
    }

    override fun dispatchIndirect(buffer: GPUBuffer, offset: Int) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}