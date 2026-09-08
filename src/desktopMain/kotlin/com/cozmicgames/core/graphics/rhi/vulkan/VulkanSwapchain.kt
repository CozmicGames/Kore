package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.utils.Disposable
import org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR
import org.lwjgl.vulkan.VK10.vkDestroyImageView
import org.lwjgl.vulkan.VkDevice

class VulkanSwapchain(private val device: VkDevice, val vulkanSwapchainObject: Long, val surfaceFormatAndSpace: VulkanColorAndDepthFormatAndSpace) : Disposable {
    var images = emptyArray<Long>()
    var imageViews = emptyArray<Long>()
    var width = 0
    var height = 0

    override fun dispose() {
        vkDestroySwapchainKHR(device, vulkanSwapchainObject, null)
        for (imageView in imageViews)
            vkDestroyImageView(device, imageView, null)
    }
}