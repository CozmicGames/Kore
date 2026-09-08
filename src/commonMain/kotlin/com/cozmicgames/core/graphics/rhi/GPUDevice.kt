package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.utils.Disposable

interface GPUDevice : Disposable{
    enum class Type {
        OTHER,
        INTEGRATED,
        DISCRETE,
        VIRTUAL,
        CPU
    }

    interface Info {
        val name: String
        val driverInfo: String
        val backendName: String
        val zZeroToOne: Boolean
        val type: Type
    }

    interface Limits {
        val maxAnisotropy: Float
        val maxTextureSize: Int
        val maxBufferSize: Int
        val maxBufferBindingPoints: Int
        val maxTextureBindingPoints: Int
        val maxColorAttachments: Int
    }

    interface DebugHandler {
        enum class Severity {
            INFO,
            ERROR,
            FAIL
        }

        fun onDebugMessage(severity: Severity, message: String)
    }

    val info: Info
    val limits: Limits

    val availableMemory: Long?
    val usedMemory: Long?

    val isDebug: Boolean
    var debugHandler: DebugHandler?

    fun createStaticBuffer(): GPUStaticBuffer
    fun createDynamicBuffer(): GPUDynamicBuffer

    fun createTexture1D(format: GPUTextureFormat): GPUTexture1D
    fun createTexture2D(format: GPUTextureFormat): GPUTexture2D
    fun createTexture3D(format: GPUTextureFormat): GPUTexture3D
    fun createTextureCube(format: GPUTextureFormat): GPUTextureCube

    fun createSampler(builder: GPUSampler.Builder.() -> Unit): GPUSampler
    fun createShader(source: GPUShaderSource): GPUShader
    fun createGraphicsPipeline(builder: GPUGraphicsPipeline.Builder.() -> Unit): GPUGraphicsPipeline
    fun createComputePipeline(builder: GPUComputePipeline.Builder.() -> Unit): GPUComputePipeline
    fun createRenderPass(builder: GPURenderPass.Builder.() -> Unit): GPURenderPass

    fun beginFrame(): GPUCommandBuffer
    fun endFrame()
}
