package com.cozmicgames.graphics.opengl

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.defaultTexture2D
import com.cozmicgames.graphics.defaultTexture3D
import com.cozmicgames.graphics.defaultTextureCube
import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.graphics.gpu.Texture3D
import com.cozmicgames.graphics.gpu.TextureCube
import com.cozmicgames.graphics.gpu.Uniform
import com.cozmicgames.utils.Disposable
import org.lwjgl.opengl.GL43C.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.*

interface GLUniform {
    fun apply()
}

class GLIntUniform(val pipeline: GLPipeline, name: String, numComponents: Int, count: Int) : Uniform<Int>(name, Type.INT, numComponents, count), GLUniform, Disposable {
    private val data = Array(numComponents * count) { 0 }
    private val buffer = memAllocInt(data.size)
    private val location = glGetUniformLocation(pipeline.shader, name)

    override fun update(block: (Array<Int>) -> Unit) {
        block(data)

        data.forEachIndexed { index, value ->
            buffer.put(index, value)
        }

        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        when (numComponents) {
            1 -> glUniform1iv(location, buffer)
            2 -> glUniform2iv(location, buffer)
            3 -> glUniform3iv(location, buffer)
            4 -> glUniform4iv(location, buffer)
        }
    }

    override fun dispose() {
        memFree(buffer)
    }
}

class GLFloatUniform(val pipeline: GLPipeline, name: String, numComponents: Int, count: Int) : Uniform<Float>(name, Type.FLOAT, numComponents, count), GLUniform, Disposable {
    private val data = Array(numComponents * count) { 0.0f }
    private val buffer = memAllocFloat(data.size)
    private val location = glGetUniformLocation(pipeline.shader, name)

    override fun update(block: (Array<Float>) -> Unit) {
        block(data)

        data.forEachIndexed { index, value ->
            buffer.put(index, value)
        }

        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        when (numComponents) {
            1 -> glUniform1fv(location, buffer)
            2 -> glUniform2fv(location, buffer)
            3 -> glUniform3fv(location, buffer)
            4 -> glUniform4fv(location, buffer)
        }
    }

    override fun dispose() {
        memFree(buffer)
    }
}

class GLMatrixUniform(val pipeline: GLPipeline, name: String, numComponents: Int, count: Int) : Uniform<Float>(name, Type.FLOAT, numComponents, count), GLUniform, Disposable {
    private val data = Array(numComponents * count) { 0.0f }
    private val buffer = memAllocFloat(data.size)
    private val location = glGetUniformLocation(pipeline.shader, name)

    override fun update(block: (Array<Float>) -> Unit) {
        block(data)

        data.forEachIndexed { index, value ->
            buffer.put(index, value)
        }

        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        when (numComponents) {
            4 -> glUniformMatrix2fv(location, false, buffer)
            9 -> glUniformMatrix3fv(location, false, buffer)
            16 -> glUniformMatrix4fv(location, false, buffer)
        }
    }

    override fun dispose() {
        memFree(buffer)
    }
}

class GLTexture2DUniform(val pipeline: GLPipeline, val slot: Int, name: String, numComponents: Int, count: Int) : Uniform<Texture2D>(name, Type.TEXTURE_2D, numComponents, count), GLUniform {
    private val values = Array(count) { Kore.graphics.defaultTexture2D }

    init {
        val previous = glGetInteger(GL_CURRENT_PROGRAM)
        glUseProgram(pipeline.shader)

        val location = if (count > 1)
            glGetUniformLocation(pipeline.shader, "$name[0]")
        else
            glGetUniformLocation(pipeline.shader, name)

        stackPush().use {
            val values = it.callocInt(count)
            repeat(count) {
                values.put(it, slot + it)
            }
            glUniform1iv(location, values)
        }

        glUseProgram(previous)
    }

    override fun update(block: (Array<Texture2D>) -> Unit) {
        block(values)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        repeat(count) {
            GLManager.bindTexture2D((values[it] as GLTexture2D).handle, slot + it)
        }
    }
}

class GLTexture3DUniform(val pipeline: GLPipeline, val slot: Int, name: String, numComponents: Int, count: Int) : Uniform<Texture3D>(name, Type.TEXTURE_2D, numComponents, count), GLUniform {
    private val values = Array(count) { Kore.graphics.defaultTexture3D }

    init {
        val previous = glGetInteger(GL_CURRENT_PROGRAM)
        glUseProgram(pipeline.shader)

        val location = if (count > 1)
            glGetUniformLocation(pipeline.shader, "$name[0]")
        else
            glGetUniformLocation(pipeline.shader, name)

        stackPush().use {
            val values = it.callocInt(count)
            repeat(count) {
                values.put(it, slot + it)
            }

            glUniform1iv(location, values)
        }

        glUseProgram(previous)
    }

    override fun update(block: (Array<Texture3D>) -> Unit) {
        block(values)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        repeat(count) {
            GLManager.bindTexture3D((values[it] as GLTexture3D).handle, slot + it)
        }
    }
}

class GLTextureCubeUniform(val pipeline: GLPipeline, val slot: Int, name: String, numComponents: Int, count: Int) : Uniform<TextureCube>(name, Type.TEXTURE_2D, numComponents, count), GLUniform {
    private val values = Array(count) { Kore.graphics.defaultTextureCube }

    init {
        val previous = glGetInteger(GL_CURRENT_PROGRAM)
        glUseProgram(pipeline.shader)

        val location = if (count > 1)
            glGetUniformLocation(pipeline.shader, "$name[0]")
        else
            glGetUniformLocation(pipeline.shader, name)

        stackPush().use {
            val values = it.callocInt(count)
            repeat(count) {
                values.put(it, slot + it)
            }

            glUniform1iv(location, values)
        }

        glUseProgram(previous)
    }

    override fun update(block: (Array<TextureCube>) -> Unit) {
        block(values)
        pipeline.setUniformUpdated(this)
    }

    override fun apply() {
        repeat(count) {
            GLManager.bindTextureCube((values[it] as GLTextureCube).handle, slot + it)
        }
    }
}
