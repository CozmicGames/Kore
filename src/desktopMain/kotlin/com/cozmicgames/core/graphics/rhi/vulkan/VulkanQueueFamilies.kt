package com.cozmicgames.core.graphics.rhi.vulkan


class VulkanQueueFamilies {
    val graphicsFamilies = arrayListOf<Int>()
    val presentFamilies = arrayListOf<Int>()

    fun findSingleSuitableQueue(): Int? {
        return graphicsFamilies.first {
            it in presentFamilies
        }
    }
}
