package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.graphics.rhi.GPUDevice
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import kotlin.use

class VulkanPhysicalDevice(val vulkanPhysicalDeviceObject: VkPhysicalDevice) {
    val name: String
    val apiVersion: String
    val driverVersion: Int
    val deviceType: GPUDevice.Type

    init {
        stackPush().use { stack ->
            val deviceProperties = VkPhysicalDeviceProperties.malloc(stack)
            vkGetPhysicalDeviceProperties(vulkanPhysicalDeviceObject, deviceProperties)

            name = deviceProperties.deviceNameString()
            apiVersion = "${VK_VERSION_MAJOR(deviceProperties.apiVersion())}.${VK_VERSION_MINOR(deviceProperties.apiVersion())}.${VK_VERSION_PATCH(deviceProperties.apiVersion())}"
            driverVersion = deviceProperties.driverVersion()
            deviceType = when (deviceProperties.deviceType()) {
                VK_PHYSICAL_DEVICE_TYPE_OTHER -> GPUDevice.Type.OTHER
                VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> GPUDevice.Type.INTEGRATED
                VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> GPUDevice.Type.DISCRETE
                VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU -> GPUDevice.Type.VIRTUAL
                VK_PHYSICAL_DEVICE_TYPE_CPU -> GPUDevice.Type.CPU
                else -> throw IllegalStateException("")
            }
        }
    }
}