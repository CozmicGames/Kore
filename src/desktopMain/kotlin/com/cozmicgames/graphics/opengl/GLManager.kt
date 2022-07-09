package com.cozmicgames.graphics.opengl

import com.cozmicgames.Kore
import com.cozmicgames.configuration
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.log
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL43C.*

object GLManager {
    val caps = GL.getCapabilities()
    val MAX_VERTEX_ATTRIBS = glGetInteger(GL_MAX_VERTEX_ATTRIBS)

    var boundVertexBuffer = 0
        private set

    var boundIndexBuffer = 0
        private set

    var boundUniformBuffer = 0
        private set

    var boundShaderStorageBuffer = 0
        private set

    var boundIndirectDrawBuffer = 0
        private set

    var boundIndirectDispatchBuffer = 0
        private set

    var boundTextures2D = IntArray(glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS))
        private set

    var boundTexturesCube = IntArray(glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS))
        private set

    var boundTextures3D = IntArray(glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS))
        private set

    var activeTextureUnit = 0
        set(value) {
            if (value == field)
                return

            glActiveTexture(GL_TEXTURE0 + value)
            field = value
        }

    var boundFramebuffer = 0
        private set

    private val enabled = hashSetOf<Int>()
    private val enabledAttribs = BooleanArray(MAX_VERTEX_ATTRIBS)

    fun bindVertexBuffer(handle: Int) {
        if (boundVertexBuffer == handle)
            return

        glBindBuffer(GL_ARRAY_BUFFER, handle)
        boundVertexBuffer = handle
    }

    fun bindIndexBuffer(handle: Int) {
        if (boundIndexBuffer == handle)
            return

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle)
        boundIndexBuffer = handle
    }

    fun bindUniformBuffer(handle: Int) {
        if (boundUniformBuffer == handle)
            return

        glBindBuffer(GL_UNIFORM_BUFFER, handle)
        boundUniformBuffer = handle
    }

    fun bindShaderStorageBuffer(handle: Int) {
        if (boundShaderStorageBuffer == handle)
            return

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, handle)
        boundShaderStorageBuffer = handle
    }

    fun bindIndirectDrawBuffer(handle: Int) {
        if (boundIndirectDrawBuffer == handle)
            return

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, handle)
        boundIndirectDrawBuffer = handle
    }

    fun bindIndirectDispatchBuffer(handle: Int) {
        if (boundIndirectDispatchBuffer == handle)
            return

        glBindBuffer(GL_DISPATCH_INDIRECT_BUFFER, handle)
        boundIndirectDispatchBuffer = handle
    }

    fun bindTexture2D(handle: Int, unit: Int = activeTextureUnit) {
        if (boundTextures2D[unit] == handle)
            return

        activeTextureUnit = unit
        glBindTexture(GL_TEXTURE_2D, handle)
        boundTextures2D[unit] = handle
    }

    fun bindTextureCube(handle: Int, unit: Int = activeTextureUnit) {
        if (boundTexturesCube[unit] == handle)
            return

        activeTextureUnit = unit
        glBindTexture(GL_TEXTURE_CUBE_MAP, handle)
        boundTexturesCube[unit] = handle
    }

    fun bindTexture3D(handle: Int, unit: Int = activeTextureUnit) {
        if (boundTextures3D[unit] == handle)
            return

        activeTextureUnit = unit
        glBindTexture(GL_TEXTURE_3D, handle)
        boundTextures3D[unit] = handle
    }

    fun bindFramebuffer(handle: Int): Boolean {
        if (boundFramebuffer == handle)
            return false

        glBindFramebuffer(GL_FRAMEBUFFER, handle)

        boundFramebuffer = handle

        return true
    }

    fun isEnabled(cap: Int) = cap in enabled

    fun enable(cap: Int) {
        if (isEnabled(cap))
            return

        glEnable(cap)
        enabled += cap
    }

    fun disable(cap: Int) {
        if (!isEnabled(cap))
            return

        glDisable(cap)
        enabled -= cap
    }

    fun enableVertexAttrib(index: Int) {
        if (enabledAttribs[index])
            return

        glEnableVertexAttribArray(index)
        enabledAttribs[index] = true
    }

    fun disableVertexAttrib(index: Int) {
        if (!enabledAttribs[index])
            return

        glDisableVertexAttribArray(index)
        enabledAttribs[index] = false
    }

    fun <R> checkErrors(block: () -> R): R {
        val result = block()

        if (Kore.configuration.debug) {
            var error = glGetError()
            while (error != GL_NO_ERROR) {
                Kore.log.error(
                    this::class, "OpenGL Error: ${
                        when (error) {
                            GL_INVALID_ENUM -> "Invalid enum"
                            GL_INVALID_VALUE -> "Invalid value"
                            GL_INVALID_OPERATION -> "Invalid operation"
                            GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid framebuffer operation"
                            GL_OUT_OF_MEMORY -> "Out of memory"
                            GL_STACK_UNDERFLOW -> "Stack underflow"
                            GL_STACK_OVERFLOW -> "Stack overflow"
                            else -> "Unknown error"
                        }
                    }"
                )
                error = glGetError()
            }
        }

        return result
    }

    fun deleteTexture2D(handle: Int) {
        glDeleteTextures(handle)

        repeat(boundTextures2D.size) {
            if (boundTextures2D[it] == handle)
                boundTextures2D[it] = 0
        }
    }

    fun deleteTextureCube(handle: Int) {
        glDeleteTextures(handle)

        repeat(boundTexturesCube.size) {
            if (boundTexturesCube[it] == handle)
                boundTexturesCube[it] = 0
        }
    }

    fun deleteTexture3D(handle: Int) {
        glDeleteTextures(handle)

        repeat(boundTextures3D.size) {
            if (boundTextures3D[it] == handle)
                boundTextures3D[it] = 0
        }
    }
}