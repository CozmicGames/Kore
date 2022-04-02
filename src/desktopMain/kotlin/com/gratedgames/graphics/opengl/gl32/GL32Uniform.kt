package com.gratedgames.graphics.opengl.gl32

import com.gratedgames.graphics.gpu.BufferUniform
import com.gratedgames.graphics.gpu.GraphicsBuffer
import com.gratedgames.graphics.opengl.GLGraphicsBuffer
import com.gratedgames.graphics.opengl.GLUniform
import com.gratedgames.utils.extensions.element
import org.lwjgl.opengl.GL32C.*

class GL32BufferUniform(val pipeline: GL32Pipeline, name: String, size: Int, val binding: Int) : BufferUniform(name, size), GLUniform {
    private val array = arrayOfNulls<GraphicsBuffer>(1)

    var buffer by array.element(0)

    override fun update(block: (Array<GraphicsBuffer?>) -> Unit) {
        block(array)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        val handle = (this.buffer as? GLGraphicsBuffer)?.handle ?: 0
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, handle)
    }
}
