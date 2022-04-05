package com.cozmicgames.graphics.opengl.gl32

import com.cozmicgames.Kore
import com.cozmicgames.graphics.IndexDataType
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.graphics.gpu.GraphicsBuffer
import com.cozmicgames.graphics.gpu.Pipeline
import com.cozmicgames.graphics.gpu.UniformBuffer
import com.cozmicgames.graphics.opengl.GLGraphicsImpl
import com.cozmicgames.log

open class GL32GraphicsImpl : GLGraphicsImpl() {
    override val uniformBufferLayout = UniformBuffer.Layout.STD140

    override val supportsCompute = false

    init {
        Kore.log.info(this::class, "Using OpenGL 3.2 functionality")
    }

    override fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit): Pipeline {
        if (type == Pipeline.Type.COMPUTE)
            Kore.log.fail(this::class, "Compute is not supported by OpenGL 3.2")

        val pipeline = GL32Pipeline(type)
        block(pipeline)
        pipeline.update()
        return pipeline
    }

    override fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int) {
        Kore.log.error(this::class, "Compute is not supported by OpenGL 3.2")
    }

    override fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int, stride: Int) {
        Kore.log.error(this::class, "Indirect drawing is not supported by OpenGL 3.2")
    }

    override fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType, count: Int, stride: Int) {
        Kore.log.error(this::class, "Indirect drawing is not supported by OpenGL 3.2")
    }

    override fun dispatchComputeIndirect(buffer: GraphicsBuffer) {
        Kore.log.error(this::class, "Indirect compute is not supported by OpenGL 3.2")
    }
}