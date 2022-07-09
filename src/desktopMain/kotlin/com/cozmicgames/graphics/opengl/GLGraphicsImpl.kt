package com.cozmicgames.graphics.opengl

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.DesktopStatistics
import com.cozmicgames.graphics.GraphicsImpl
import com.cozmicgames.graphics.IndexDataType
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.extensions.emptyIntArray
import org.lwjgl.opengl.GL32C.*

abstract class GLGraphicsImpl : GraphicsImpl {
    internal var currentPipeline: GLPipeline? = null

    private var currentLayout: VertexLayout? = null
    private val globalVertexArray = glGenVertexArrays()

    override fun initialize() {
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_TEXTURE_3D)
        glEnable(GL_TEXTURE_CUBE_MAP)
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS)
        glEnable(GL_PROGRAM_POINT_SIZE)
    }

    override fun beginFrame() {
        setFramebuffer(null)
        setViewport(null)
        glBindVertexArray(globalVertexArray)
    }

    override fun endFrame() {
        glBindVertexArray(0)
    }

    override fun createBuffer(usage: GraphicsBuffer.Usage, block: GraphicsBuffer.() -> Unit): GraphicsBuffer {
        val buffer = GLGraphicsBuffer(usage)
        block(buffer)
        return buffer
    }

    override fun createTexture2D(format: Texture.Format, sampler: Sampler, block: Texture2D.() -> Unit): Texture2D {
        val texture = GLTexture2D(format, sampler)
        block(texture)
        return texture
    }

    override fun createTextureCube(format: Texture.Format, sampler: Sampler, block: TextureCube.() -> Unit): TextureCube {
        val texture = GLTextureCube(format, sampler)
        block(texture)
        return texture
    }

    override fun createTexture3D(format: Texture.Format, sampler: Sampler, block: Texture3D.() -> Unit): Texture3D {
        val texture = GLTexture3D(format, sampler)
        block(texture)
        return texture
    }

    override fun createFramebuffer(block: Framebuffer.() -> Unit): Framebuffer {
        val framebuffer = GLFramebuffer()
        block(framebuffer)
        return framebuffer
    }

    override fun createSampler(block: Sampler.() -> Unit): Sampler {
        val sampler = GLSampler()
        block(sampler)
        return sampler
    }

    override fun setPipeline(pipeline: Pipeline?) {
        glUseProgram((pipeline as? GLPipeline?)?.shader ?: 0)

        glDepthMask(pipeline?.depthMask ?: true)
        glStencilMask(pipeline?.stencilMask ?: 0xFFFFFFFF.toInt())

        val colorMaskR = pipeline?.colorMask?.r ?: true
        val colorMaskG = pipeline?.colorMask?.g ?: true
        val colorMaskB = pipeline?.colorMask?.b ?: true
        val colorMaskA = pipeline?.colorMask?.a ?: true
        glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA)

        if (pipeline?.blendState == null)
            GLManager.disable(GL_BLEND)
        else
            pipeline.blendState?.let {
                GLManager.enable(GL_BLEND)

                val sFactor = when (it.srcFactor) {
                    Pipeline.BlendState.Factor.ZERO -> GL_ZERO
                    Pipeline.BlendState.Factor.ONE -> GL_ONE
                    Pipeline.BlendState.Factor.SOURCE_COLOR -> GL_SRC_COLOR
                    Pipeline.BlendState.Factor.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                    Pipeline.BlendState.Factor.DEST_COLOR -> GL_DST_COLOR
                    Pipeline.BlendState.Factor.ONE_MINUS_DEST_COLOR -> GL_ONE_MINUS_DST_COLOR
                    Pipeline.BlendState.Factor.SOURCE_ALPHA -> GL_SRC_ALPHA
                    Pipeline.BlendState.Factor.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                    Pipeline.BlendState.Factor.DEST_ALPHA -> GL_DST_ALPHA
                    Pipeline.BlendState.Factor.ONE_MINUS_DEST_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                }

                val dFactor = when (it.destFactor) {
                    Pipeline.BlendState.Factor.ZERO -> GL_ZERO
                    Pipeline.BlendState.Factor.ONE -> GL_ONE
                    Pipeline.BlendState.Factor.SOURCE_COLOR -> GL_SRC_COLOR
                    Pipeline.BlendState.Factor.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                    Pipeline.BlendState.Factor.DEST_COLOR -> GL_DST_COLOR
                    Pipeline.BlendState.Factor.ONE_MINUS_DEST_COLOR -> GL_ONE_MINUS_DST_COLOR
                    Pipeline.BlendState.Factor.SOURCE_ALPHA -> GL_SRC_ALPHA
                    Pipeline.BlendState.Factor.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                    Pipeline.BlendState.Factor.DEST_ALPHA -> GL_DST_ALPHA
                    Pipeline.BlendState.Factor.ONE_MINUS_DEST_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                }

                glBlendFunc(sFactor, dFactor)

                val glEquation = when (it.equation) {
                    Pipeline.BlendState.Equation.ADD -> GL_FUNC_ADD
                    Pipeline.BlendState.Equation.SUBTRACT -> GL_FUNC_SUBTRACT
                    Pipeline.BlendState.Equation.REVERSE_SUBTRACT -> GL_FUNC_REVERSE_SUBTRACT
                    Pipeline.BlendState.Equation.MIN -> GL_MIN
                    Pipeline.BlendState.Equation.MAX -> GL_MAX
                }

                glBlendEquation(glEquation)
            }

        if (pipeline?.depthState == null)
            GLManager.disable(GL_DEPTH_TEST)
        else
            pipeline.depthState?.let {
                GLManager.enable(GL_DEPTH_TEST)

                glDepthRange(it.min.toDouble(), it.max.toDouble())

                val func = when (it.func) {
                    Pipeline.DepthState.Func.ALWAYS -> GL_ALWAYS
                    Pipeline.DepthState.Func.LESS -> GL_LESS
                    Pipeline.DepthState.Func.LESS_OR_EQUAL -> GL_LEQUAL
                    Pipeline.DepthState.Func.EQUAL -> GL_EQUAL
                    Pipeline.DepthState.Func.GREATER_OR_EQUAL -> GL_GEQUAL
                    Pipeline.DepthState.Func.GREATER -> GL_GREATER
                    Pipeline.DepthState.Func.NOT_EQUAL -> GL_NOTEQUAL
                    Pipeline.DepthState.Func.NEVER -> GL_NEVER
                }

                glDepthFunc(func)
            }

        if (pipeline?.stencilState == null)
            GLManager.disable(GL_STENCIL_TEST)
        else
            pipeline.stencilState?.let {
                GLManager.enable(GL_STENCIL_TEST)

                val func = when (it.func) {
                    Pipeline.StencilState.Func.ALWAYS -> GL_ALWAYS
                    Pipeline.StencilState.Func.LESS -> GL_LESS
                    Pipeline.StencilState.Func.LEQUAL -> GL_LEQUAL
                    Pipeline.StencilState.Func.EQUAL -> GL_EQUAL
                    Pipeline.StencilState.Func.GEQUAL -> GL_GEQUAL
                    Pipeline.StencilState.Func.GREATER -> GL_GREATER
                    Pipeline.StencilState.Func.NOT_EQUAL -> GL_NOTEQUAL
                    Pipeline.StencilState.Func.NEVER -> GL_NEVER
                }

                glStencilFunc(func, it.ref, it.mask)

                val sFail = when (it.stencilFail) {
                    Pipeline.StencilState.Operation.KEEP -> GL_KEEP
                    Pipeline.StencilState.Operation.ZERO -> GL_ZERO
                    Pipeline.StencilState.Operation.INCREMENT -> GL_INCR
                    Pipeline.StencilState.Operation.DECREMENT -> GL_DECR
                    Pipeline.StencilState.Operation.INVERT -> GL_INVERT
                    Pipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                }

                val dFail = when (it.stencilFail) {
                    Pipeline.StencilState.Operation.KEEP -> GL_KEEP
                    Pipeline.StencilState.Operation.ZERO -> GL_ZERO
                    Pipeline.StencilState.Operation.INCREMENT -> GL_INCR
                    Pipeline.StencilState.Operation.DECREMENT -> GL_DECR
                    Pipeline.StencilState.Operation.INVERT -> GL_INVERT
                    Pipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                }

                val dPass = when (it.stencilFail) {
                    Pipeline.StencilState.Operation.KEEP -> GL_KEEP
                    Pipeline.StencilState.Operation.ZERO -> GL_ZERO
                    Pipeline.StencilState.Operation.INCREMENT -> GL_INCR
                    Pipeline.StencilState.Operation.DECREMENT -> GL_DECR
                    Pipeline.StencilState.Operation.INVERT -> GL_INVERT
                    Pipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                }

                glStencilOp(sFail, dFail, dPass)
            }

        if (pipeline?.cullState == null)
            GLManager.disable(GL_CULL_FACE)
        else
            pipeline.cullState?.let {
                GLManager.enable(GL_CULL_FACE)

                val face = when {
                    it.front && !it.back -> GL_FRONT
                    it.back && !it.front -> GL_BACK
                    it.back && it.front -> GL_FRONT_AND_BACK
                    else -> GL_NONE
                }

                glCullFace(face)
            }

        repeat(GLManager.MAX_VERTEX_ATTRIBS) {
            if (it in (pipeline?.vertexLayout?.indices ?: emptyIntArray()))
                GLManager.enableVertexAttrib(it)
            else
                GLManager.disableVertexAttrib(it)
        }

        (pipeline as GLPipeline?)?.let {
            it.applyOnSetPipelineUniforms.forEach {
                it.apply()
            }
        }

        currentLayout = pipeline?.vertexLayout
        currentPipeline = pipeline
    }

    override fun setFramebuffer(framebuffer: Framebuffer?) {
        GLManager.bindFramebuffer((framebuffer as? GLFramebuffer?)?.handle ?: 0)
    }

    override fun setScissor(rect: ScissorRect?) {
        if (rect == null)
            GLManager.disable(GL_SCISSOR_TEST)
        else {
            GLManager.enable(GL_SCISSOR_TEST)
            glScissor(rect.x, rect.y, rect.width, rect.height)
        }
    }

    override fun setViewport(viewport: Viewport?) {
        val x = viewport?.x ?: 0
        val y = viewport?.y ?: 0
        val width = viewport?.width ?: Kore.graphics.width
        val height = viewport?.height ?: Kore.graphics.height

        glViewport(x, y, width, height)
    }

    override fun setVertexBuffer(buffer: GraphicsBuffer?, indices: IntArray) {
        if (buffer != null) {
            GLManager.bindVertexBuffer((buffer as GLGraphicsBuffer).handle)

            currentLayout?.let {
                for (index in indices) {
                    val attribute = it.attributes[index]

                    val glType = when (attribute.type) {
                        VertexLayout.AttributeType.BYTE -> if (attribute.isNormalized) GL_UNSIGNED_BYTE else GL_BYTE
                        VertexLayout.AttributeType.SHORT -> if (attribute.isNormalized) GL_UNSIGNED_SHORT else GL_SHORT
                        VertexLayout.AttributeType.INT -> if (attribute.isNormalized) GL_UNSIGNED_INT else GL_INT
                        VertexLayout.AttributeType.FLOAT -> GL_FLOAT
                    }

                    if (attribute.asInt)
                        glVertexAttribIPointer(index, attribute.count, glType, it.stride, it.offsets[index].toLong())
                    else
                        glVertexAttribPointer(index, attribute.count, glType, attribute.isNormalized, it.stride, it.offsets[index].toLong())
                }
            }
        } else
            GLManager.bindVertexBuffer(0)
    }

    override fun setIndexBuffer(buffer: GraphicsBuffer?) {
        if (buffer != null)
            GLManager.bindIndexBuffer((buffer as GLGraphicsBuffer).handle)
        else
            GLManager.bindIndexBuffer(0)
    }

    override fun clear(color: Color?, depth: Float?, stencil: Int?) {
        var mask = 0

        if (color != null) {
            glClearColor(color.r, color.g, color.b, color.a)
            mask = mask or GL_COLOR_BUFFER_BIT
        }

        if (depth != null) {
            glClearDepth(depth.toDouble())
            mask = mask or GL_DEPTH_BUFFER_BIT
        }

        if (stencil != null) {
            glClearStencil(stencil)
            mask = mask or GL_STENCIL_BUFFER_BIT
        }

        if (mask != 0)
            glClear(mask)
    }

    override fun draw(primitive: Primitive, length: Int, offset: Int, numInstances: Int) {
        currentPipeline?.updateUniforms()

        val glPrimitive = primitive.toGLPrimitive()

        GLManager.checkErrors {
            if (numInstances > 1)
                glDrawArraysInstanced(glPrimitive, offset, length, numInstances)
            else
                glDrawArrays(glPrimitive, offset, length)
        }

        DesktopStatistics.numDrawCalls++
        DesktopStatistics.renderedPrimitives[primitive.ordinal] += length
    }

    override fun drawIndexed(primitive: Primitive, length: Int, offset: Int, type: IndexDataType, numInstances: Int) {
        currentPipeline?.updateUniforms()

        val glPrimitive = primitive.toGLPrimitive()
        val glType = type.toGLIndexDataType()

        GLManager.checkErrors {
            if (numInstances > 1)
                glDrawElementsInstanced(glPrimitive, length, glType, offset.toLong(), numInstances)
            else
                glDrawElements(glPrimitive, length, glType, offset.toLong())
        }

        DesktopStatistics.numDrawCalls++
        DesktopStatistics.renderedPrimitives[primitive.ordinal] += length
    }

    override fun dispose() {
        glDeleteVertexArrays(globalVertexArray)
    }
}