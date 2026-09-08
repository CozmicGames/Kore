package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.utils.Disposable
import org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT
import org.lwjgl.vulkan.VkInstance

class VulkanDebugCallbackAndHandle(private val instance: VkInstance, val messengerHandle: Long, val callback: VkDebugUtilsMessengerCallbackEXT): Disposable {
    override fun dispose() {
        vkDestroyDebugUtilsMessengerEXT(instance, messengerHandle, null)
        callback.free()
    }
}
