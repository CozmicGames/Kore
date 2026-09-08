package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.graphics.rhi.GPUShader
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.graphics.SpirVCompiledShader
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

class GLProgram(device: GLDevice, shader: GPUShaderSource) : GPUShader {
    override val resources: List<GPUShader.Resource>

    val handle get() = handleInternal

    private var handleInternal = 0

    init {
        shader as? SpirVCompiledShader ?: throw IllegalArgumentException("Invalid shader type: ${shader::class}")

        resources = shader.resources

        handleInternal = glCreateProgram()

        for ((stage, spirvData) in shader.data) {
            val glShader = glCreateShader(stage.toGLType())

            val spirv = MemoryUtil.memCalloc(spirvData.size)
            spirv.put(spirvData)
            spirv.flip()

            try {
                stackPush().use { stack ->
                    glShaderBinary(stack.ints(glShader), GL_SHADER_BINARY_FORMAT_SPIR_V, spirv)
                }

                glSpecializeShader(glShader, "main", null as IntBuffer?, null)

                device.checkFail(glGetShaderi(glShader, GL_COMPILE_STATUS) == GL_TRUE) {
                    "Failed to specialize $stage shader:\n${glGetShaderInfoLog(glShader)}"
                }

                glAttachShader(handleInternal, glShader)
            } finally {
                glDeleteShader(glShader)
                MemoryUtil.memFree(spirv)
            }
        }

        glLinkProgram(handleInternal)

        device.checkFail(glGetProgrami(handleInternal, GL_LINK_STATUS) == GL_TRUE) {
            "Failed to link program:\n${glGetProgramInfoLog(handleInternal)}"
        }
    }

    override fun dispose() {
        if (handleInternal != 0) {
            glDeleteProgram(handleInternal)
            handleInternal = 0
        }
    }
}