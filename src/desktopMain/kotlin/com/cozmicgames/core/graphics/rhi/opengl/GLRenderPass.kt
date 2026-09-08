package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPURenderPass
import com.cozmicgames.core.graphics.rhi.GPUTexture2D
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.utils.Color
import org.lwjgl.opengl.GL46C.*

class GLRenderPass(private val device: GLDevice, private val builder: Builder) : GPURenderPass {
    class ColorAttachment(val texture: GPUTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearColor: Color)
    class DepthAttachment(val texture: GPUTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearDepth: Float)
    class StencilAttachment(val texture: GPUTexture2D, val level: Int, val layer: Int, val loadOp: GPURenderPass.LoadOp, val storeOp: GPURenderPass.StoreOp, val clearStencil: Int)

    class Builder : GPURenderPass.Builder {
        internal val colorAttachments = ArrayList<ColorAttachment>()
        internal var depthAttachment: DepthAttachment? = null
        internal var stencilAttachment: StencilAttachment? = null

        override fun addColorAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearColor: Color) {
            colorAttachments += ColorAttachment(texture, level, layer, loadOp, storeOp, clearColor)
        }

        override fun setDepthAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearDepth: Float) {
            depthAttachment = DepthAttachment(texture, level, layer, loadOp, storeOp, clearDepth)
        }

        override fun setStencilAttachment(texture: GPUTexture2D, level: Int, layer: Int, loadOp: GPURenderPass.LoadOp, storeOp: GPURenderPass.StoreOp, clearStencil: Int) {
            stencilAttachment = StencilAttachment(texture, level, layer, loadOp, storeOp, clearStencil)
        }
    }

    internal val colorAttachments = builder.colorAttachments.toList()
    internal val depthAttachment = builder.depthAttachment
    internal val stencilAttachment = builder.stencilAttachment

    internal val handle get() = handleInternal
    private var handleInternal = 0

    init {
        handleInternal = glCreateFramebuffers()

        for (i in builder.colorAttachments.indices) {
            val attachment = builder.colorAttachments[i]
            val texture = attachment.texture as GLTexture2D

            if (attachment.layer >= 0)
                glNamedFramebufferTextureLayer(handleInternal, GL_COLOR_ATTACHMENT0 + i, texture.handle, attachment.level, attachment.layer)
            else
                glNamedFramebufferTexture(handleInternal, GL_COLOR_ATTACHMENT0 + i, texture.handle, attachment.level)
        }

        builder.depthAttachment?.let {
            val texture = it.texture as GLTexture2D

            if (it.layer >= 0)
                glNamedFramebufferTextureLayer(handleInternal, GL_DEPTH_ATTACHMENT, texture.handle, it.level, it.layer)
            else
                glNamedFramebufferTexture(handleInternal, GL_DEPTH_ATTACHMENT, texture.handle, it.level)
        }

        builder.stencilAttachment?.let {
            val texture = it.texture as GLTexture2D

            if (it.layer >= 0)
                glNamedFramebufferTextureLayer(handleInternal, GL_STENCIL_ATTACHMENT, texture.handle, it.level, it.layer)
            else
                glNamedFramebufferTexture(handleInternal, GL_STENCIL_ATTACHMENT, texture.handle, it.level)
        }

        if (builder.colorAttachments.isEmpty()) {
            glNamedFramebufferDrawBuffer(handleInternal, GL_NONE)
            glNamedFramebufferReadBuffer(handleInternal, GL_NONE)
        } else
            glNamedFramebufferDrawBuffers(handleInternal, IntArray(builder.colorAttachments.size) { GL_COLOR_ATTACHMENT0 + it })

        val result = glCheckNamedFramebufferStatus(handleInternal, GL_FRAMEBUFFER)
        device.checkFail(result == GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is incomplete: ${
                when (result) {
                    GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED"
                    GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"
                    GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"
                    GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"
                    GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"
                    GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED"
                    GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"
                    GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS"
                    else -> "Unknown status: 0x${result.toString(16)}"
                }
            }"
        }

        DesktopStatistics.numRenderPasses++
    }

    override fun dispose() {
        if (handleInternal != 0) {
            glDeleteFramebuffers(handleInternal)
            handleInternal = 0
            DesktopStatistics.numRenderPasses--
        }
    }
}