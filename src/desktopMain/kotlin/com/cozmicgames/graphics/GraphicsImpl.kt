package com.cozmicgames.graphics

import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable

interface GraphicsImpl : Disposable {
    val supportsCompute: Boolean
    val uniformBufferLayout: UniformBuffer.Layout

    fun initialize()
    fun beginFrame()
    fun endFrame()

    fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit): Pipeline
    fun createBuffer(usage: GraphicsBuffer.Usage, block: GraphicsBuffer.() -> Unit): GraphicsBuffer
    fun createTexture2D(format: Texture.Format, block: Texture2D.() -> Unit): Texture2D
    fun createTextureCube(format: Texture.Format, block: TextureCube.() -> Unit): TextureCube
    fun createTexture3D(format: Texture.Format, block: Texture3D.() -> Unit): Texture3D
    fun createFramebuffer(block: Framebuffer.() -> Unit): Framebuffer
    fun setPipeline(pipeline: Pipeline?)
    fun setFramebuffer(framebuffer: Framebuffer?)
    fun setScissor(rect: ScissorRect?)
    fun setViewport(viewport: Viewport?)
    fun setVertexBuffer(buffer: GraphicsBuffer?, indices: IntArray)
    fun setIndexBuffer(buffer: GraphicsBuffer?)
    fun clear(color: Color?, depth: Float?, stencil: Int?)
    fun draw(primitive: Primitive, length: Int, offset: Int, numInstances: Int)
    fun drawIndexed(primitive: Primitive, length: Int, offset: Int, type: IndexDataType, numInstances: Int)
    fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int)
    fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int, stride: Int)
    fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType, count: Int, stride: Int)
    fun dispatchComputeIndirect(buffer: GraphicsBuffer)
}