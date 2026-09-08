package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.Kore
import com.cozmicgames.core.configuration
import com.cozmicgames.core.log
import org.lwjgl.opengl.GL43C.*

object GLManager {
    var boundIndirectDrawBuffer = 0
        private set

    var boundIndirectDispatchBuffer = 0
        private set

    var boundFramebuffer = 0
        private set

    var boundProgram = 0
        private set

    private val enabled = hashSetOf<Int>()

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

    fun bindFramebuffer(handle: Int): Boolean {
        if (boundFramebuffer == handle)
            return false

        glBindFramebuffer(GL_FRAMEBUFFER, handle)

        boundFramebuffer = handle

        return true
    }

    fun bindProgram(handle: Int): Boolean {
        if (boundProgram == handle)
            return false

        glUseProgram(handle)

        boundProgram = handle

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

    fun <R> checkErrors(block: () -> R): R {
        val result = block()

        if (Kore.configuration.debug) {
            var error = glGetError()
            while (error != GL_NO_ERROR) {
                Kore.log.fail(
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
}