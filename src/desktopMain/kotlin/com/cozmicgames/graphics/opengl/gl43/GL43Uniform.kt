package com.cozmicgames.graphics.opengl.gl43

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.defaultTexture2D
import com.cozmicgames.graphics.defaultTexture3D
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.opengl.*
import com.cozmicgames.utils.extensions.element
import org.lwjgl.opengl.GL43C.*

class GLImage2DUniform(val pipeline: GLPipeline, val slot: Int, name: String) : Image2DUniform(name), GLUniform {
    private val values = Array(count) { Kore.graphics.defaultTexture2D at 0 }

    init {
        val previous = glGetInteger(GL_CURRENT_PROGRAM)
        glUseProgram(pipeline.shader)
        glUniform1i(glGetUniformLocation(pipeline.shader, name), slot)
        glUseProgram(previous)
    }

    override fun update(block: (Array<TextureImage2D>) -> Unit) {
        block(values)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        val image = values[0]
        glBindImageTexture(slot, (image.texture as GLTexture2D).handle, image.level, false, 0, GL_READ_WRITE, image.texture.format.toInternalGLFormat())
    }
}

class GLImage3DUniform(val pipeline: GLPipeline, val slot: Int, name: String) : Image3DUniform(name), GLUniform {
    private val values = Array(count) { Kore.graphics.defaultTexture3D at 0 }

    init {
        val previous = glGetInteger(GL_CURRENT_PROGRAM)
        glUseProgram(pipeline.shader)
        glUniform1i(glGetUniformLocation(pipeline.shader, name), slot)
        glUseProgram(previous)
    }

    override fun update(block: (Array<TextureImage3D>) -> Unit) {
        block(values)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        val image = values[0]
        glBindImageTexture(slot, (image.texture as GLTexture3D).handle, image.level, false, 0, GL_READ_WRITE, image.texture.format.toInternalGLFormat())
    }
}

class GL43BufferUniform(val pipeline: GL43Pipeline, name: String, size: Int, val binding: Int) : BufferUniform(name, size), GLUniform {
    private val array = arrayOfNulls<GraphicsBuffer>(1)

    var buffer by array.element(0)

    override fun update(block: (Array<GraphicsBuffer?>) -> Unit) {
        block(array)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        val handle = (this.buffer as? GLGraphicsBuffer)?.handle ?: 0
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, handle)
    }
}
