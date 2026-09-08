package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.Kore
import com.cozmicgames.core.applicationInfo
import com.cozmicgames.core.configuration
import com.cozmicgames.core.graphics.rhi.*
import com.cozmicgames.core.utils.Disposable
import org.lwjgl.glfw.GLFWVulkan.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR
import org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
import org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR
import org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR
import org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR
import org.lwjgl.vulkan.VK10.vkCreateFence
import org.lwjgl.vulkan.VK10.vkCreateSemaphore
import org.lwjgl.vulkan.VK14.*


class VulkanDevice(selectedPhysicalDeviceName: String?, windowHandle: Long) : GPUDevice, Disposable {
    override val info = object : GPUDevice.Info {
        override val name get() = selectedPhysicalDevice.physicalDevice.name
        override val driverInfo get() = "Driver version ${selectedPhysicalDevice.physicalDevice.driverVersion}"
        override val backendName = "Vulkan"
        override val zZeroToOne: Boolean
            get() = TODO("Not yet implemented")
        override val type get() = selectedPhysicalDevice.physicalDevice.deviceType
    }
    override val limits: GPUDevice.Limits
        get() = TODO("Not yet implemented")
    override val availableMemory: Long?
        get() = TODO("Not yet implemented")
    override val usedMemory: Long?
        get() = TODO("Not yet implemented")
    override val isDebug: Boolean
    override var debugHandler: GPUDevice.DebugHandler?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun createStaticBuffer(): GPUStaticBuffer {
        TODO("Not yet implemented")
    }

    override fun createDynamicBuffer(): GPUDynamicBuffer {
        TODO("Not yet implemented")
    }

    override fun createTexture1D(format: GPUTextureFormat): GPUTexture1D {
        TODO("Not yet implemented")
    }

    override fun createTexture2D(format: GPUTextureFormat): GPUTexture2D {
        TODO("Not yet implemented")
    }

    override fun createTexture3D(format: GPUTextureFormat): GPUTexture3D {
        TODO("Not yet implemented")
    }

    override fun createTextureCube(format: GPUTextureFormat): GPUTextureCube {
        TODO("Not yet implemented")
    }

    override fun createSampler(builder: GPUSampler.Builder.() -> Unit): GPUSampler {
        TODO("Not yet implemented")
    }

    override fun createShader(source: GPUShaderSource): GPUShader {
        TODO("Not yet implemented")
    }

    override fun createGraphicsPipeline(builder: GPUGraphicsPipeline.Builder.() -> Unit): GPUGraphicsPipeline {
        TODO("Not yet implemented")
    }

    override fun createComputePipeline(builder: GPUComputePipeline.Builder.() -> Unit): GPUComputePipeline {
        TODO("Not yet implemented")
    }

    override fun createRenderPass(builder: GPURenderPass.Builder.() -> Unit): GPURenderPass {
        TODO("Not yet implemented")
    }

    /**
     *
     */
    private val instance: VkInstance
    private val debugCallbackAndHandle: VulkanDebugCallbackAndHandle?
    private val surface: Long
    private val selectedPhysicalDevice: VulkanSelectedPhysicalDevice
    internal val device: VkDevice
    private val queue: VkQueue
    internal var swapchain: VulkanSwapchain
    private val commandPool: Long

    /**
     *
     */
    internal var currentFrameIndex = 0
    internal var currentImageIndex = 0
    private var needsRecreate = false
    private var imageAcquireSemaphores = LongArray(0)
    private var renderCompleteSemaphores = LongArray(0)
    private var renderFences = LongArray(0)
    private var imagesInFlight = LongArray(0)
    private var frameCommandBuffers = emptyArray<VulkanCommandBuffer>()


    init {
        if (!glfwVulkanSupported())
            throw AssertionError() //TODO

        isDebug = Kore.applicationInfo.isDebug

        val requiredInstanceExtensions = glfwGetRequiredInstanceExtensions() ?: throw AssertionError()//TODO
        instance = VulkanUtils.createInstance(isDebug, Kore.applicationInfo, requiredInstanceExtensions)
        surface = VulkanUtils.createSurface(instance, windowHandle)
        debugCallbackAndHandle = if (isDebug) VulkanUtils.setupDebugging(instance) else null
        selectedPhysicalDevice = VulkanUtils.selectPhysicalDevice(VulkanUtils.enumeratePhysicalDevices(instance), selectedPhysicalDeviceName, surface)
        device = VulkanUtils.createDevice(Kore.applicationInfo, selectedPhysicalDevice.physicalDevice, listOf(VK_KHR_SWAPCHAIN_EXTENSION_NAME), selectedPhysicalDevice.queueFamilyIndex)
        queue = VulkanUtils.retrieveQueue(device, selectedPhysicalDevice.queueFamilyIndex)
        swapchain = VulkanUtils.createSwapchain(device, selectedPhysicalDevice, surface, null, Kore.configuration)
        commandPool = VulkanUtils.createCommandPool(device, 0, selectedPhysicalDevice.queueFamilyIndex)

        createFrameObjects()
    }

    private fun destroyFrameObjects() {
        for (semaphore in imageAcquireSemaphores)
            vkDestroySemaphore(device, semaphore, null)

        for (semaphore in renderCompleteSemaphores)
            vkDestroySemaphore(device, semaphore, null)

        for (fence in renderFences)
            vkDestroyFence(device, fence, null)

        for (commandBuffer in frameCommandBuffers)
            vkFreeCommandBuffers(device, commandPool, commandBuffer.vulkanCommandBufferObject)
    }

    private fun createFrameObjects() {
        imageAcquireSemaphores = LongArray(swapchain.imageViews.size)
        renderCompleteSemaphores = LongArray(swapchain.imageViews.size)
        renderFences = LongArray(swapchain.imageViews.size)
        imagesInFlight = LongArray(swapchain.imageViews.size) { VK_NULL_HANDLE }

        for (i in swapchain.imageViews.indices) {
            stackPush().use { stack ->
                val pSemaphore = stack.mallocLong(1)
                val pSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack)
                pSemaphoreCreateInfo.`sType$Default`()

                VulkanUtils.checkVulkanResult(vkCreateSemaphore(device, pSemaphoreCreateInfo, null, pSemaphore), "Failed to create image acquire semaphore")
                imageAcquireSemaphores[i] = pSemaphore.get(0)

                VulkanUtils.checkVulkanResult(vkCreateSemaphore(device, pSemaphoreCreateInfo, null, pSemaphore), "Failed to create render complete semaphore")
                renderCompleteSemaphores[i] = pSemaphore.get(0)

                val pFence = stack.mallocLong(1)
                val pFenceCreateInfo = VkFenceCreateInfo.calloc(stack)
                pFenceCreateInfo.`sType$Default`()
                pFenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT)

                VulkanUtils.checkVulkanResult(vkCreateFence(device, pFenceCreateInfo, null, pFence), "Failed to create fence")
                renderFences[i] = pFence.get(0)
            }
        }

        frameCommandBuffers = Array(swapchain.imageViews.size) {
            stackPush().use { stack ->
                val pCommandBuffer = stack.mallocPointer(1)
                val pAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                pAllocateInfo.`sType$Default`()
                pAllocateInfo.commandPool(commandPool)
                pAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                pAllocateInfo.commandBufferCount(1)

                VulkanUtils.checkVulkanResult(vkAllocateCommandBuffers(device, pAllocateInfo, pCommandBuffer), "Failed to allocate command buffer")
                VulkanCommandBuffer(this, VkCommandBuffer(pCommandBuffer.get(0), device))
            }
        }
    }

    private fun acquireNextImage(): Int? {
        stackPush().use { stack ->
            val pImageIndex = stack.mallocInt(1)
            val result = vkAcquireNextImageKHR(device, swapchain.vulkanSwapchainObject, Long.MAX_VALUE, imageAcquireSemaphores[currentFrameIndex], VK_NULL_HANDLE, pImageIndex)
            return if (result != VK_ERROR_OUT_OF_DATE_KHR) pImageIndex.get(0) else null
        }
    }

    fun resize() {
        needsRecreate = true
    }



    override fun beginFrame(): GPUCommandBuffer {
        VulkanUtils.checkVulkanResult(vkWaitForFences(device, renderFences[currentFrameIndex], true, Long.MAX_VALUE), "Failed to wait for fence")

        var imageIndex = acquireNextImage()

        if (imageIndex == null)
            needsRecreate = true

        if (needsRecreate) {
            VulkanUtils.checkVulkanResult(vkDeviceWaitIdle(device), "Failed to wait for device idle")

            swapchain = VulkanUtils.createSwapchain(device, selectedPhysicalDevice, surface, swapchain, Kore.configuration)
            destroyFrameObjects()
            createFrameObjects()
            currentFrameIndex = 0

            needsRecreate = false
        }

        imageIndex = acquireNextImage()

        if (imageIndex == null)
            throw java.lang.AssertionError()

        currentImageIndex = imageIndex

        if (imagesInFlight[currentImageIndex] != VK_NULL_HANDLE)
            VulkanUtils.checkVulkanResult(vkWaitForFences(device, imagesInFlight[currentImageIndex], true, Long.MAX_VALUE), "Failed to wait for image fence")

        imagesInFlight[currentImageIndex] = renderFences[currentFrameIndex]
        VulkanUtils.checkVulkanResult(vkResetFences(device, renderFences[currentFrameIndex]), "Failed to reset fence")

        val currentCommandBuffer = frameCommandBuffers[currentImageIndex]

        stackPush().use { stack ->
            val pBeginInfo = VkCommandBufferBeginInfo.calloc(stack)
            pBeginInfo.`sType$Default`()

            VulkanUtils.checkVulkanResult(vkBeginCommandBuffer(currentCommandBuffer.vulkanCommandBufferObject, pBeginInfo), "Failed to begin command buffer")
        }

        return currentCommandBuffer
    }

    override fun endFrame() {
        val currentCommandBuffer = frameCommandBuffers[currentImageIndex]

        VulkanUtils.checkVulkanResult(vkEndCommandBuffer(currentCommandBuffer.vulkanCommandBufferObject), "Failed to end command buffer")

        stackPush().use { stack ->
            val pSubmitInfo = VkSubmitInfo.calloc(stack)
            pSubmitInfo.`sType$Default`()
            pSubmitInfo.waitSemaphoreCount(1)
            pSubmitInfo.pWaitSemaphores(stack.longs(imageAcquireSemaphores[currentFrameIndex]))
            pSubmitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
            pSubmitInfo.pCommandBuffers(stack.pointers(currentCommandBuffer.vulkanCommandBufferObject))
            pSubmitInfo.pSignalSemaphores(stack.longs(renderCompleteSemaphores[currentImageIndex]))

            VulkanUtils.checkVulkanResult(vkQueueSubmit(queue, pSubmitInfo, renderFences[currentFrameIndex]), "Failed to submit raster command buffer")

            val pPresentInfo = VkPresentInfoKHR.calloc(stack)
            pPresentInfo.`sType$Default`()
            pPresentInfo.pWaitSemaphores(stack.longs(renderCompleteSemaphores[currentImageIndex]))
            pPresentInfo.swapchainCount(1)
            pPresentInfo.pSwapchains(stack.longs(swapchain.vulkanSwapchainObject))
            pPresentInfo.pImageIndices(stack.ints(currentImageIndex))

            val result: Int = vkQueuePresentKHR(queue, pPresentInfo)
            needsRecreate = result != VK_ERROR_OUT_OF_DATE_KHR && result != VK_SUBOPTIMAL_KHR
        }

        currentFrameIndex = (currentFrameIndex + 1) % swapchain.imageViews.size
    }

    override fun dispose() {
        destroyFrameObjects()

        vkDestroyCommandPool(device, commandPool, null)
        swapchain.dispose()
        vkDestroyDevice(device, null)
        debugCallbackAndHandle?.dispose()
        vkDestroyInstance(instance, null)
    }
}