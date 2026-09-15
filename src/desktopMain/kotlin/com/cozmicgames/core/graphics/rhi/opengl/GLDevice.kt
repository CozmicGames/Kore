package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUCommandBuffer
import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.graphics.rhi.GPUComputePipeline
import com.cozmicgames.core.graphics.rhi.GPUDevice
import com.cozmicgames.core.graphics.rhi.GPUDynamicBuffer
import com.cozmicgames.core.graphics.rhi.GPUGraphicsPipeline
import com.cozmicgames.core.graphics.rhi.GPURenderPass
import com.cozmicgames.core.graphics.rhi.GPUSampler
import com.cozmicgames.core.graphics.rhi.GPUStaticBuffer
import com.cozmicgames.core.graphics.rhi.GPUTexture1D
import com.cozmicgames.core.graphics.rhi.GPUTexture2D
import com.cozmicgames.core.graphics.rhi.GPUTexture3D
import com.cozmicgames.core.graphics.rhi.GPUTextureCube
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.GPUShader
import org.lwjgl.opengl.ATIMeminfo.GL_VBO_FREE_MEMORY_ATI
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*
import org.lwjgl.opengl.GLDebugMessageCallback
import org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX
import org.lwjgl.system.MemoryStack.*

class GLDevice(override val isDebug: Boolean) : GPUDevice {
    override val info = object : GPUDevice.Info {
        private val vendor = glGetString(GL_VENDOR) ?: ""
        private val renderer = glGetString(GL_RENDERER) ?: ""
        private val version = glGetString(GL_VERSION) ?: ""
        private val glslVersion = glGetString(GL_SHADING_LANGUAGE_VERSION) ?: ""

        override val name = renderer.ifBlank { "OpenGL" }

        override val driverInfo: String = buildString {
            if (vendor.isNotBlank())
                append(vendor)

            if (version.isNotBlank()) {
                if (isNotEmpty()) append(" - ")
                append(version)
            }

            if (glslVersion.isNotBlank()) {
                if (isNotEmpty()) append(" - ")
                append("GLSL ")
                append(glslVersion)
            }
        }

        override val backendName = "OpenGL"

        override val zZeroToOne: Boolean = false

        override val type: GPUDevice.Type = run {
            val text = "$vendor $renderer".lowercase()
            when {
                // NVIDIA GPUs
                "nvidia" in text -> GPUDevice.Type.DISCRETE
                // Intel integrated GPUs
                "intel" in text && "arc" !in text -> GPUDevice.Type.INTEGRATED
                // Intel Arc discrete GPUs
                "intel" in text && "arc" in text -> GPUDevice.Type.DISCRETE
                // AMD discrete GPUs
                "amd" in text && ("radeon rx" in text || "radeon pro" in text || "radeon vii" in text) -> GPUDevice.Type.DISCRETE
                // AMD integrated GPUs
                "amd" in text && ("radeon graphics" in text || "radeon vega" in text) -> GPUDevice.Type.INTEGRATED
                else -> GPUDevice.Type.OTHER
            }
        }
    }

    override val limits = object : GPUDevice.Limits {
        override val maxAnisotropy = if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY) else 1.0f

        override val maxTextureSize: Int = glGetInteger(GL_MAX_TEXTURE_SIZE)

        override val maxBufferSize: Int = glGetInteger(GL_MAX_SHADER_STORAGE_BLOCK_SIZE)

        override val maxBufferBindingPoints: Int = glGetInteger(GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS)

        override val maxTextureBindingPoints: Int = glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS)

        override val maxColorAttachments: Int = glGetInteger(GL_MAX_COLOR_ATTACHMENTS)
    }

    override val availableMemory: Long?
    override val usedMemory get() = usedMemoryInternal
    override var debugHandler: GPUDevice.DebugHandler? = null

    internal var usedMemoryInternal = 0L

    private val placeboVao: Int
    private val commandBuffer = GLCommandBuffer()

    init {
        val capabilities = GL.getCapabilities()

        if (isDebug) {
            glEnable(GL_DEBUG_OUTPUT)
            glDebugMessageCallback({ source, type, id, severity, length, message, userParam ->
                val msg = GLDebugMessageCallback.getMessage(length, message)
                val sev = when (severity) {
                    GL_DEBUG_SEVERITY_HIGH -> GPUDevice.DebugHandler.Severity.FAIL
                    GL_DEBUG_SEVERITY_MEDIUM -> GPUDevice.DebugHandler.Severity.ERROR
                    GL_DEBUG_SEVERITY_LOW -> GPUDevice.DebugHandler.Severity.INFO
                    else -> null
                }

                if (sev != null)
                    debugHandler?.onDebugMessage(sev, msg)
            }, 0L)
        }

        availableMemory = when {
            capabilities.GL_NVX_gpu_memory_info -> glGetInteger(GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX).toLong() * 1024L
            capabilities.GL_ATI_meminfo -> stackPush().use {
                val pMemory = it.callocInt(2)
                glGetIntegerv(GL_VBO_FREE_MEMORY_ATI, pMemory)
                pMemory.get(0).toLong() * 1024L
            }

            else -> null
        }

        placeboVao = glGenVertexArrays()
        glBindVertexArray(placeboVao)
        glFrontFace(GL_CCW)
    }

    override fun createStaticBuffer(): GPUStaticBuffer {
        return GLStaticBuffer(this)
    }

    override fun createDynamicBuffer(): GPUDynamicBuffer {
        return GLDynamicBuffer(this)
    }

    override fun createTexture1D(format: GPUTextureFormat): GPUTexture1D {
        return GLTexture1D(this, format)
    }

    override fun createTexture2D(format: GPUTextureFormat): GPUTexture2D {
        return GLTexture2D(this, format)
    }

    override fun createTexture3D(format: GPUTextureFormat): GPUTexture3D {
        return GLTexture3D(this, format)
    }

    override fun createTextureCube(format: GPUTextureFormat): GPUTextureCube {
        return GLTextureCube(this, format)
    }

    override fun createSampler(builder: GPUSampler.Builder.() -> Unit): GPUSampler {
        val builder = GLSampler.Builder()
        builder.builder()
        return GLSampler(this, builder)
    }

    override fun createShader(source: GPUShaderSource): GPUShader {
        return GLProgram(this, source)
    }

    override fun createGraphicsPipeline(builder: GPUGraphicsPipeline.Builder.() -> Unit): GPUGraphicsPipeline {
        val builder = GLGraphicsPipeline.Builder()
        builder.builder()
        return GLGraphicsPipeline(this, builder)
    }

    override fun createComputePipeline(builder: GPUComputePipeline.Builder.() -> Unit): GPUComputePipeline {
        val builder = GLComputePipeline.Builder()
        builder.builder()
        return GLComputePipeline(this, builder)
    }

    override fun createRenderPass(builder: GPURenderPass.Builder.() -> Unit): GPURenderPass {
        val builder = GLRenderPass.Builder()
        builder.builder()
        return GLRenderPass(this, builder)
    }

    override fun beginFrame(): GPUCommandBuffer {
        return commandBuffer
    }

    override fun endFrame() {
        commandBuffer.execute()
    }

    override fun dispose() {
        glDeleteVertexArrays(placeboVao)
    }
}