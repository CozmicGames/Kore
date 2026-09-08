package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.graphics.Primitive
import com.cozmicgames.core.utils.Color
import com.cozmicgames.core.utils.Disposable

interface GPUCommandBuffer : Disposable {
    fun beginMainRenderPass(clearColor: Color, clearDepth: Float)
    fun endMainRenderPass()
    fun beginRenderPass(renderPass: GPURenderPass)
    fun endRenderPass()
    fun setViewport(x: Int, y: Int, width: Int, height: Int)
    fun setScissor(x: Int, y: Int, width: Int, height: Int)
    fun setPipeline(pipeline: GPUPipeline)
    fun setBuffer(binding: Int, buffer: GPUBuffer)
    fun setTexture(binding: Int, texture: GPUTexture)
    fun setSampler(binding: Int, sampler: GPUSampler)
    fun setImage(binding: Int, texture: GPUTexture, level: Int, layer: Int, access: GPUImageAccess)
    fun draw(primitive: Primitive, count: Int, instanceCount: Int = 1, firstVertex: Int = 0, firstInstance: Int = 0)
    fun drawIndirect(primitive: Primitive, buffer: GPUBuffer, offset: Int = 0, drawCount: Int = 1, stride: Int = 0)
    fun dispatch(x: Int, y: Int = 1, z: Int = 1)
    fun dispatchIndirect(buffer: GPUBuffer, offset: Int = 0)
}
