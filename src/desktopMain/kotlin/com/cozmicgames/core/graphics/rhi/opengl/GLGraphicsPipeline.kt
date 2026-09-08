package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.graphics.rhi.GPUGraphicsPipeline
import com.cozmicgames.core.graphics.rhi.GPUShader
import com.cozmicgames.core.graphics.rhi.internal.checkFail

class GLGraphicsPipeline(device: GLDevice, builder: Builder) : GPUGraphicsPipeline() {
    class Builder : GPUGraphicsPipeline.Builder {
        internal var shader: GPUShader? = null
        internal var colorMask: ColorMask? = null
        internal var depthMask: Boolean? = null
        internal var stencilMask: Int? = null
        internal var depthState: DepthState? = null
        internal var stencilState: StencilState? = null
        internal var blendState: BlendState? = null
        internal var cullState: CullState? = null

        override fun setShader(shader: GPUShader) {
            this.shader = shader
        }

        override fun setColorMask(mask: ColorMask.() -> Unit) {
            colorMask = ColorMask(mask)
        }

        override fun setDepthMask(mask: Boolean) {
            depthMask = mask
        }

        override fun setStencilMask(mask: Int) {
            stencilMask = mask
        }

        override fun setDepthState(state: DepthState.() -> Unit) {
            depthState = DepthState(state)
        }

        override fun setStencilState(state: StencilState.() -> Unit) {
            stencilState = StencilState(state)
        }

        override fun setBlendState(state: BlendState.() -> Unit) {
            blendState = BlendState(state)
        }

        override fun setCullState(state: CullState.() -> Unit) {
            cullState = CullState(state)
        }
    }

    override val shader = builder.shader as GLProgram

    internal val colorMask = builder.colorMask
    internal val depthMask = builder.depthMask
    internal val stencilMask = builder.stencilMask
    internal val depthState = builder.depthState
    internal val stencilState = builder.stencilState
    internal val blendState = builder.blendState
    internal val cullState = builder.cullState

    override val id get() = this@GLGraphicsPipeline.shader.handle

    init {
        device.checkFail(builder.shader != null) {
            "Pipeline creation requires a shader"
        }

        DesktopStatistics.numPipelines++
    }

    override fun dispose() {
        DesktopStatistics.numPipelines--
    }
}