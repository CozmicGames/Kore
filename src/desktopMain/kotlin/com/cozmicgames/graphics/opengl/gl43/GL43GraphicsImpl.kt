package com.cozmicgames.graphics.opengl.gl43

import com.cozmicgames.Kore
import com.cozmicgames.graphics.DesktopStatistics
import com.cozmicgames.graphics.IndexDataType
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.opengl.*
import com.cozmicgames.log
import org.lwjgl.opengl.GL43C.*

open class GL43GraphicsImpl : GLGraphicsImpl() {
    override val uniformBufferLayout = UniformBuffer.Layout.STD430

    override val supportsCompute = true

    init {
        Kore.log.info(this::class, "Using OpenGL 4.3 functionality")
    }

    override fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit): Pipeline {
        val pipeline = GL43Pipeline(type)
        block(pipeline)
        pipeline.update()
        return pipeline
    }

    private fun getMemoryBarrierMask(): Int {
        var barrierMask = 0

        currentPipeline?.let {
            it.uniforms.forEach { name ->
                val uniform = it.getUniform<Any>(name)

                if (uniform is TextureUniform) {
                    barrierMask = barrierMask or GL_TEXTURE_FETCH_BARRIER_BIT
                    barrierMask = barrierMask or GL_TEXTURE_UPDATE_BARRIER_BIT
                }

                if (uniform is Image2DUniform || uniform is Image3DUniform)
                    barrierMask = barrierMask or GL_SHADER_IMAGE_ACCESS_BARRIER_BIT

                if (uniform is BufferUniform)
                    barrierMask = barrierMask or GL_BUFFER_UPDATE_BARRIER_BIT
            }
        }

        return barrierMask
    }

    override fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int) {
        currentPipeline?.updateUniforms()

        GLManager.checkErrors {
            glDispatchCompute(groupsX, groupsY, groupsZ)
        }

        val barrierMask = getMemoryBarrierMask()

        if (barrierMask != 0)
            glMemoryBarrier(barrierMask)

        DesktopStatistics.numComputeDispatches++
    }

    override fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int, stride: Int) {
        currentPipeline?.updateUniforms()

        val glPrimitive = primitive.toGLPrimitive()

        GLManager.bindIndirectDrawBuffer((buffer as GLGraphicsBuffer).handle)
        GLManager.checkErrors {
            glMultiDrawArraysIndirect(glPrimitive, 0L, count, stride)
        }

        DesktopStatistics.numDrawCalls++
    }

    override fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType, count: Int, stride: Int) {
        currentPipeline?.updateUniforms()

        val glPrimitive = primitive.toGLPrimitive()
        val glType = type.toGLIndexDataType()

        GLManager.bindIndirectDrawBuffer((buffer as GLGraphicsBuffer).handle)
        GLManager.checkErrors {
            glMultiDrawElementsIndirect(glPrimitive, glType, 0L, count, stride)
        }

        DesktopStatistics.numDrawCalls++
    }

    override fun dispatchComputeIndirect(buffer: GraphicsBuffer) {
        currentPipeline?.updateUniforms()

        GLManager.bindIndirectDispatchBuffer((buffer as GLGraphicsBuffer).handle)
        GLManager.checkErrors {
            glDispatchComputeIndirect(0L)
        }

        val barrierMask = getMemoryBarrierMask()

        if (barrierMask != 0)
            glMemoryBarrier(barrierMask)

        DesktopStatistics.numComputeDispatches++
    }
}