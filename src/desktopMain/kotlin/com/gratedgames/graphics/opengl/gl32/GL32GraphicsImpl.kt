package com.gratedgames.graphics.opengl.gl32

import com.gratedgames.Kore
import com.gratedgames.graphics.IndexDataType
import com.gratedgames.graphics.Primitive
import com.gratedgames.graphics.gpu.GraphicsBuffer
import com.gratedgames.graphics.gpu.Pipeline
import com.gratedgames.graphics.gpu.UniformBuffer
import com.gratedgames.graphics.opengl.GLGraphicsImpl
import com.gratedgames.log

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