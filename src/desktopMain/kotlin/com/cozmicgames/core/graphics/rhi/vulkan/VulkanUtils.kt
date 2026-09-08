package com.cozmicgames.core.graphics.rhi.vulkan

import com.cozmicgames.core.Configuration
import com.cozmicgames.core.Kore
import com.cozmicgames.core.ApplicationInfo
import com.cozmicgames.core.graphics.rhi.GPUDevice
import com.cozmicgames.core.log
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR
import org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR
import org.lwjgl.vulkan.VK10.vkCreateCommandPool
import org.lwjgl.vulkan.VK14.*
import java.nio.ByteBuffer
import java.util.Collections.emptyList
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.toList


object VulkanUtils {
    private const val MAX_FRAMES_IN_FLIGHT = 3
    private const val KHRONOS_VALIDATION_LAYER_NAME = "VK_LAYER_KHRONOS_validation"

    fun translateVulkanResult(result: Int): String {
        return when (result) {
            VK_SUCCESS -> "Command successfully completed"
            VK_NOT_READY -> "A fence or query has not yet completed"
            VK_TIMEOUT -> "A wait operation has not completed in the specified time"
            VK_EVENT_SET -> "An event is signaled"
            VK_EVENT_RESET -> "An event is unsignaled"
            VK_INCOMPLETE -> "A return array was too small for the result"
            VK_ERROR_OUT_OF_HOST_MEMORY -> "A host memory allocation has failed"
            VK_ERROR_OUT_OF_DEVICE_MEMORY -> "A device memory allocation has failed"
            VK_ERROR_INITIALIZATION_FAILED -> "Initialization of an object could not be completed for implementation-specific reasons"
            VK_ERROR_DEVICE_LOST -> "The logical or physical device has been lost"
            VK_ERROR_MEMORY_MAP_FAILED -> "Mapping of a memory object has failed"
            VK_ERROR_LAYER_NOT_PRESENT -> "A requested layer is not present or could not be loaded"
            VK_ERROR_EXTENSION_NOT_PRESENT -> "A requested extension is not supported"
            VK_ERROR_FEATURE_NOT_PRESENT -> "A requested feature is not supported"
            VK_ERROR_INCOMPATIBLE_DRIVER -> "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons"
            VK_ERROR_TOO_MANY_OBJECTS -> "Too many objects of the type have already been created"
            VK_ERROR_FORMAT_NOT_SUPPORTED -> "A requested format is not supported on this device"
            VK_ERROR_FRAGMENTED_POOL -> "A pool allocation has failed due to fragmentation of the pool's memory"
            else -> String.format("Unknown error [%d]", result)
        }
    }

    fun checkVulkanResult(result: Int, message: String) {
        if (result != VK_SUCCESS)
            throw AssertionError("$message: ${translateVulkanResult(result)}")
    }

    private fun pointers(stack: MemoryStack, pts: PointerBuffer, vararg pointers: ByteBuffer): PointerBuffer {
        val res = stack.mallocPointer(pts.remaining() + pointers.size)
        res.put(pts)
        for (pointer in pointers)
            res.put(pointer)
        res.flip()
        return res
    }

    private fun enumerateSupportedInstanceExtensions(): List<String> {
        stackPush().use { stack ->
            val pPropertyCount = stack.mallocInt(1)
            checkVulkanResult(vkEnumerateInstanceExtensionProperties(null as CharSequence?, pPropertyCount, null), "Failed to enumerate instance extensions")

            val propertyCount = pPropertyCount.get(0)
            val pProperties = VkExtensionProperties.malloc(propertyCount, stack)

            checkVulkanResult(vkEnumerateInstanceExtensionProperties(null as CharSequence?, pPropertyCount, pProperties), "Failed to enumerate instance extensions")

            return pProperties.stream().map { it.extensionNameString() }.toList()
        }
    }

    private fun enumerateSupportedInstanceLayers(): List<String> {
        stackPush().use { stack ->
            val pPropertyCount = stack.mallocInt(1)
            checkVulkanResult(vkEnumerateInstanceLayerProperties(pPropertyCount, null), "Failed to enumerate instance layers")

            val count = pPropertyCount.get(0)

            if (count > 0) {
                val pProperties = VkLayerProperties.malloc(count, stack)
                checkVulkanResult(vkEnumerateInstanceLayerProperties(pPropertyCount, pProperties), "Failed to enumerate instance layers")
                return pProperties.stream().map { it.layerNameString() }.toList()
            }
        }

        return emptyList()
    }


    fun createInstance(isDebug: Boolean, applicationInfo: ApplicationInfo, requiredExtensions: PointerBuffer): VkInstance {
        val supportedInstanceExtensions = enumerateSupportedInstanceExtensions()

        stackPush().use { stack ->
            val appInfo = VkApplicationInfo.calloc(stack)
            appInfo.`sType$Default`()
            appInfo.pApplicationName(stack.UTF8(applicationInfo.applicationName))
            val appVersion = applicationInfo.applicationVersion
            if (appVersion != null)
                appInfo.applicationVersion(VK_MAKE_VERSION(appVersion.major, appVersion.minor, appVersion.patch))
            else
                appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0))
            appInfo.pEngineName(stack.UTF8(applicationInfo.engineName ?: "Unknown Engine"))
            val engineVersion = applicationInfo.engineVersion
            if (engineVersion != null)
                appInfo.engineVersion(VK_MAKE_VERSION(engineVersion.major, engineVersion.minor, engineVersion.patch))
            else
                appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0))
            appInfo.apiVersion(VK_API_VERSION_1_4)

            var ppEnabledExtensionNames: PointerBuffer? = requiredExtensions
            if (isDebug) {
                if (VK_EXT_DEBUG_UTILS_EXTENSION_NAME !in supportedInstanceExtensions)
                    throw AssertionError("$VK_EXT_DEBUG_UTILS_EXTENSION_NAME is not supported on the instance")

                ppEnabledExtensionNames = pointers(stack, requiredExtensions, stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
            }

            var ppEnabledLayerNames: PointerBuffer? = null
            if (isDebug) {
                val supportedLayers = enumerateSupportedInstanceLayers()
                if (KHRONOS_VALIDATION_LAYER_NAME !in supportedLayers)
                    System.err.println("DEBUG requested but layer $KHRONOS_VALIDATION_LAYER_NAME is unavailable. Install the Vulkan SDK for your platform. Vulkan debug layer will not be used.")
                else
                    ppEnabledLayerNames = stack.pointers(stack.UTF8(KHRONOS_VALIDATION_LAYER_NAME))
            }

            val instanceCreateInfo = VkInstanceCreateInfo.calloc(stack)
            instanceCreateInfo.`sType$Default`()
            instanceCreateInfo.pApplicationInfo(appInfo)
            instanceCreateInfo.ppEnabledExtensionNames(ppEnabledExtensionNames)
            instanceCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames)

            val pInstance = stack.mallocPointer(1)
            checkVulkanResult(vkCreateInstance(instanceCreateInfo, null, pInstance), "Failed to create VkInstance")

            return VkInstance(pInstance.get(0), instanceCreateInfo)
        }
    }

    fun enumeratePhysicalDevices(instance: VkInstance): List<VulkanPhysicalDevice> {
        stackPush().use { stack ->
            val pPhysicalDeviceCount = stack.mallocInt(1)
            checkVulkanResult(vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null), "Failed to enumerate physical devices")

            val physicalDeviceCount = pPhysicalDeviceCount.get(0)

            if (physicalDeviceCount == 0)
                throw AssertionError("No physical devices found")

            val pPhysicalDevices = stack.mallocPointer(physicalDeviceCount)
            checkVulkanResult(vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices), "Failed to enumerate physical devices")

            return (0 until physicalDeviceCount).map { VulkanPhysicalDevice(VkPhysicalDevice(pPhysicalDevices.get(it), instance)) }
        }
    }

    fun createSurface(instance: VkInstance, windowHandle: Long): Long {
        stackPush().use { stack ->
            val surface = stack.mallocLong(1)
            checkVulkanResult(glfwCreateWindowSurface(instance, windowHandle, null, surface), "Failed to create surface")
            return surface.get(0)
        }
    }

    fun obtainQueueFamilies(physicalDevice: VkPhysicalDevice, surface: Long): VulkanQueueFamilies? {
        val result = VulkanQueueFamilies()
        stackPush().use { stack ->
            val pQueueFamilyPropertyCount = stack.mallocInt(1)
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null)
            val numQueueFamilies = pQueueFamilyPropertyCount.get(0)
            if (numQueueFamilies == 0)
                return null

            val familyProperties = VkQueueFamilyProperties.malloc(numQueueFamilies, stack)
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, familyProperties)
            var queueFamilyIndex = 0
            val pSupported = stack.mallocInt(1)
            for (queueFamilyProps in familyProperties) {
                if (queueFamilyProps.queueCount() < 1)
                    continue

                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, queueFamilyIndex, surface, pSupported)

                if (familySupports(queueFamilyProps, VK_QUEUE_GRAPHICS_BIT))
                    result.graphicsFamilies.add(queueFamilyIndex)

                if (pSupported.get(0) != 0)
                    result.presentFamilies.add(queueFamilyIndex)

                queueFamilyIndex++
            }
            return result
        }
    }

    private fun familySupports(prop: VkQueueFamilyProperties, bit: Int): Boolean {
        return (prop.queueFlags() and bit) != 0
    }

    fun setupDebugging(instance: VkInstance): VulkanDebugCallbackAndHandle? {
        val callback = object : VkDebugUtilsMessengerCallbackEXT() {
            override fun invoke(messageSeverity: Int, messageTypes: Int, pCallbackData: Long, pUserData: Long): Int {
                val message = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
                Kore.log.error(this::class, message.pMessageString() ?: "")
                return 0
            }
        }

        stackPush().use { stack ->
            val pCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
            pCreateInfo.`sType$Default`()
            pCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
            pCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
            pCreateInfo.pfnUserCallback(callback)

            val pMessenger = stack.mallocLong(1)
            checkVulkanResult(vkCreateDebugUtilsMessengerEXT(instance, pCreateInfo, null, pMessenger), "Failed to create debug messenger")

            return VulkanDebugCallbackAndHandle(instance, pMessenger.get(0), callback)
        }
    }

    fun createDevice(applicationInfo: ApplicationInfo, physicalDevice: VulkanPhysicalDevice, requiredExtensions: List<String>, queueFamilyIndex: Int): VkDevice {
        val supportedDeviceExtensions = enumerateDeviceExtensions(physicalDevice.vulkanPhysicalDeviceObject)
        for (requiredExtension in requiredExtensions)
            if (!supportedDeviceExtensions.contains(requiredExtension))
                throw AssertionError("$requiredExtension device extension is not supported")

        stackPush().use { stack ->
            val extensions = stack.mallocPointer(requiredExtensions.size)
            for (requiredExtension in requiredExtensions) extensions.put(stack.UTF8(requiredExtension))
            extensions.flip()
            var ppEnabledLayerNames: PointerBuffer? = null
            if (applicationInfo.isDebug)
                ppEnabledLayerNames = stack.pointers(stack.UTF8(KHRONOS_VALIDATION_LAYER_NAME))

            val queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1, stack)
            queueCreateInfo.`sType$Default`()
            queueCreateInfo.queueFamilyIndex(queueFamilyIndex)
            queueCreateInfo.pQueuePriorities(stack.floats(1.0f))

            val pDynamicRenderingFeatures = VkPhysicalDeviceDynamicRenderingFeatures.calloc(stack)
            pDynamicRenderingFeatures.`sType$Default`()
            pDynamicRenderingFeatures.dynamicRendering(true)

            val pCreateInfo = VkDeviceCreateInfo.calloc(stack)
            pCreateInfo.`sType$Default`()
            pCreateInfo.pQueueCreateInfos(queueCreateInfo)
            pCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames)
            pCreateInfo.ppEnabledExtensionNames(extensions)
            pCreateInfo.pNext(pDynamicRenderingFeatures)
            val pDevice = stack.mallocPointer(1)
            checkVulkanResult(vkCreateDevice(physicalDevice.vulkanPhysicalDeviceObject, pCreateInfo, null, pDevice), "Failed to create device")
            return VkDevice(pDevice.get(0), physicalDevice.vulkanPhysicalDeviceObject, pCreateInfo, VK_API_VERSION_1_3)
        }
    }


    fun enumerateDeviceExtensions(physicalDevice: VkPhysicalDevice): List<String> {
        stackPush().use { stack ->
            val pPropertyCount = stack.mallocInt(1)
            checkVulkanResult(vkEnumerateDeviceExtensionProperties(physicalDevice, null as CharSequence?, pPropertyCount, null), "Failed to enumerate device extensions")

            val propertyCount = pPropertyCount.get(0)
            val pProperties = VkExtensionProperties.malloc(propertyCount, stack)

            checkVulkanResult(vkEnumerateDeviceExtensionProperties(physicalDevice, null as CharSequence?, pPropertyCount, pProperties), "Failed to enumerate device extensions")

            return pProperties.stream().map { it.extensionNameString() }.toList()
        }
    }

    fun selectPhysicalDevice(devices: List<VulkanPhysicalDevice>, selectedName: String? = null, surface: Long): VulkanSelectedPhysicalDevice {
        var best: VulkanSelectedPhysicalDevice? = null

        if (selectedName != null) {
            val device = devices.find { it.name == selectedName }
            if (device != null) {
                val queueFamilies = obtainQueueFamilies(device.vulkanPhysicalDeviceObject, surface)
                val selectedQueueFamilyIndex = queueFamilies?.findSingleSuitableQueue()
                if (selectedQueueFamilyIndex != null)
                    best = VulkanSelectedPhysicalDevice(device, selectedQueueFamilyIndex)
            }
        }

        if (best == null) {
            for (device in devices) {
                if (best == null || device.deviceType == GPUDevice.Type.DISCRETE) {
                    val queueFamilies = obtainQueueFamilies(device.vulkanPhysicalDeviceObject, surface)
                    val selectedQueueFamilyIndex = queueFamilies?.findSingleSuitableQueue()

                    if (selectedQueueFamilyIndex != null)
                        best = VulkanSelectedPhysicalDevice(device, selectedQueueFamilyIndex)
                }
            }
        }

        if (best == null)
            throw AssertionError("No suitable physical device found")

        return best
    }


    private fun getSupportedDepthFormat(physicalDevice: VulkanPhysicalDevice): Int? {
        val depthFormats = intArrayOf(
            VK_FORMAT_D32_SFLOAT_S8_UINT,
            VK_FORMAT_D32_SFLOAT,
            VK_FORMAT_D24_UNORM_S8_UINT,
            VK_FORMAT_D16_UNORM_S8_UINT,
            VK_FORMAT_D16_UNORM
        )

        stackPush().use { stack ->
            val formatProperties = VkFormatProperties.calloc(stack)

            for (format in depthFormats) {
                vkGetPhysicalDeviceFormatProperties(physicalDevice.vulkanPhysicalDeviceObject, format, formatProperties)

                if ((formatProperties.optimalTilingFeatures() and VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0)
                    return format
            }
        }

        return null
    }


    private fun determineSurfaceFormat(physicalDevice: VulkanPhysicalDevice, surface: Long): VulkanColorAndDepthFormatAndSpace {
        val preferredFormat = VK_FORMAT_B8G8R8A8_SRGB
        val preferredSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR

        val depthFormat = getSupportedDepthFormat(physicalDevice)

        if (depthFormat == null)
            throw AssertionError("No supported depth format")

        stackPush().use { stack ->
            val pSurfaceFormatCount = stack.mallocInt(1)
            checkVulkanResult(vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.vulkanPhysicalDeviceObject, surface, pSurfaceFormatCount, null), "Failed to get number of device surface formats")
            val pSurfaceFormats = VkSurfaceFormatKHR
                .malloc(pSurfaceFormatCount.get(0), stack)
            checkVulkanResult(vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.vulkanPhysicalDeviceObject, surface, pSurfaceFormatCount, pSurfaceFormats), "Failed to get device surface formats")
            for (surfaceFormat in pSurfaceFormats)
                if (surfaceFormat.format() == preferredFormat && surfaceFormat.colorSpace() == preferredSpace)
                    return VulkanColorAndDepthFormatAndSpace(surfaceFormat.format(), depthFormat, surfaceFormat.colorSpace())

            return VulkanColorAndDepthFormatAndSpace(pSurfaceFormats.get(0).format(), depthFormat, pSurfaceFormats.get(0).colorSpace())
        }
    }


    fun createSwapchain(device: VkDevice, selectedPhysicalDevice: VulkanSelectedPhysicalDevice, surface: Long, oldSwapchain: VulkanSwapchain? = null, configuration: Configuration): VulkanSwapchain {
        stackPush().use { stack ->
            val pSurfaceCapabilities: VkSurfaceCapabilitiesKHR = VkSurfaceCapabilitiesKHR.malloc(stack)
            checkVulkanResult(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(selectedPhysicalDevice.physicalDevice.vulkanPhysicalDeviceObject, surface, pSurfaceCapabilities), "Failed to get physical device surface capabilities")
            val pPresentModeCount = stack.mallocInt(1)
            checkVulkanResult(vkGetPhysicalDeviceSurfacePresentModesKHR(selectedPhysicalDevice.physicalDevice.vulkanPhysicalDeviceObject, surface, pPresentModeCount, null), "Failed to get presentation modes count")
            val presentModeCount = pPresentModeCount.get(0)
            val pPresentModes = stack.mallocInt(presentModeCount)
            checkVulkanResult(vkGetPhysicalDeviceSurfacePresentModesKHR(selectedPhysicalDevice.physicalDevice.vulkanPhysicalDeviceObject, surface, pPresentModeCount, pPresentModes), "Failed to get presentation modes")

            var imageCount = max(pSurfaceCapabilities.minImageCount(), MAX_FRAMES_IN_FLIGHT)
            if (pSurfaceCapabilities.maxImageCount() > 0)
                imageCount = min(imageCount, pSurfaceCapabilities.maxImageCount())

            val surfaceFormat = determineSurfaceFormat(selectedPhysicalDevice.physicalDevice, surface)

            val currentExtent = pSurfaceCapabilities.currentExtent()
            val swapchainWidth = if (currentExtent.width() == -1) max(min(configuration.width, pSurfaceCapabilities.maxImageExtent().width()), pSurfaceCapabilities.minImageExtent().width()) else currentExtent.width()
            val swapchainHeight = if (currentExtent.height() == -1) max(min(configuration.height, pSurfaceCapabilities.maxImageExtent().height()), pSurfaceCapabilities.minImageExtent().height()) else currentExtent.height()


            val pSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
            pSwapchainCreateInfo.`sType$Default`()
            pSwapchainCreateInfo.surface(surface)
            pSwapchainCreateInfo.minImageCount(imageCount)
            pSwapchainCreateInfo.imageFormat(surfaceFormat.colorFormat)
            pSwapchainCreateInfo.imageColorSpace(surfaceFormat.colorSpace)
            pSwapchainCreateInfo.imageExtent({
                it.width(swapchainWidth)
                it.height(swapchainHeight)
            })
            pSwapchainCreateInfo.imageArrayLayers(1)
            pSwapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
            pSwapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            pSwapchainCreateInfo.preTransform(pSurfaceCapabilities.currentTransform())
            pSwapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
            pSwapchainCreateInfo.presentMode(VK_PRESENT_MODE_FIFO_KHR)
            pSwapchainCreateInfo.clipped(true)
            pSwapchainCreateInfo.oldSwapchain(oldSwapchain?.vulkanSwapchainObject ?: VK_NULL_HANDLE)
            pSwapchainCreateInfo

            val pSwapchain = stack.mallocLong(1)
            checkVulkanResult(vkCreateSwapchainKHR(device, pSwapchainCreateInfo, null, pSwapchain), "Failed to create swap chain")

            //TODO: Maybe move this
            oldSwapchain?.dispose()

            val swapchain = VulkanSwapchain(device, pSwapchain.get(0), surfaceFormat)

            val pSwapchainImageCount = stack.mallocInt(1)
            checkVulkanResult(vkGetSwapchainImagesKHR(device, swapchain.vulkanSwapchainObject, pSwapchainImageCount, null), "Failed to get swapchain images count")
            val actualImageCount = pSwapchainImageCount.get(0)
            val pSwapchainImages = stack.mallocLong(actualImageCount)
            checkVulkanResult(vkGetSwapchainImagesKHR(device, swapchain.vulkanSwapchainObject, pSwapchainImageCount, pSwapchainImages), "Failed to get swapchain images")
            swapchain.images = Array<Long>(actualImageCount) { 0L }
            swapchain.imageViews = Array<Long>(actualImageCount) { 0L }

            val pImageView = stack.mallocLong(1)

            repeat(actualImageCount) { index ->
                val image = pSwapchainImages.get(index)
                swapchain.images[index] = image

                val pCreateInfo = VkImageViewCreateInfo.calloc(stack)
                pCreateInfo.`sType$Default`()
                pCreateInfo.image(image)
                pCreateInfo.viewType(VK_IMAGE_TYPE_2D)
                pCreateInfo.format(surfaceFormat.colorFormat)
                pCreateInfo.subresourceRange({
                    it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    it.layerCount(1)
                    it.levelCount(1)
                })

                checkVulkanResult(vkCreateImageView(device, pCreateInfo, null, pImageView), "Failed to create image view")

                swapchain.imageViews[index] = pImageView.get(0)
            }

            return swapchain
        }
    }

    fun retrieveQueue(device: VkDevice, queueFamilyIndex: Int): VkQueue {
        stackPush().use { stack ->
            val pQueue = stack.mallocPointer(1)
            vkGetDeviceQueue(device, queueFamilyIndex, 0, pQueue)
            return VkQueue(pQueue.get(0), device)
        }
    }

    fun createCommandPool(device: VkDevice, flags: Int, queueFamilyIndex: Int): Long {
        stackPush().use { stack ->
            val pCreateInfo = VkCommandPoolCreateInfo.calloc(stack)
            pCreateInfo.`sType$Default`()
            pCreateInfo.flags(flags)
            pCreateInfo.queueFamilyIndex(queueFamilyIndex)

            val pCmdPool = stack.mallocLong(1)
            checkVulkanResult(vkCreateCommandPool(device, pCreateInfo, null, pCmdPool), "Failed to create command pool")

            return pCmdPool.get(0)
        }
    }
}