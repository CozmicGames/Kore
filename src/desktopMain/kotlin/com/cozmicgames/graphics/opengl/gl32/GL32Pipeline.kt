package com.cozmicgames.graphics.opengl.gl32

import com.cozmicgames.Kore
import com.cozmicgames.graphics.gpu.Uniform
import com.cozmicgames.graphics.gpu.pipeline.StageType
import com.cozmicgames.graphics.gpu.pipeline.contains
import com.cozmicgames.graphics.opengl.*
import com.cozmicgames.log
import com.cozmicgames.utils.maths.Vector3i
import org.lwjgl.opengl.GL32C.*
import org.lwjgl.system.MemoryStack

class GL32Pipeline(override val type: Type) : GLPipeline() {
    override var shader = 0

    override val workgroupSizes get() = Vector3i.ZERO

    override fun update() {
        if (programSource == null) {
            Kore.log.info(this::class, "Pipeline program source must not be null")
            return
        }

        fun createShaderStage(source: String, type: Int): Int {
            val handle = glCreateShader(type)
            glShaderSource(handle, source)
            glCompileShader(handle)
            if (glGetShaderi(handle, GL_COMPILE_STATUS) != GL_TRUE)
                Kore.log.fail(this::class, buildString {
                    append("Failed to compile shader: ${glGetShaderInfoLog(handle)}\n")
                    append("Source:\n")
                    source.lines().forEachIndexed { index, line ->
                        append("$index: $line\n")
                    }
                })
            return handle
        }

        val shaders = arrayListOf<Int>()

        programSource?.let {
            if (StageType.VERTEX in it)
                shaders += createShaderStage(GLShaderGenerator.createVertexSource(it, vertexLayout, false), GL_VERTEX_SHADER)

            if (StageType.GEOMETRY in it)
                shaders += createShaderStage(GLShaderGenerator.createGeometrySource(it, false), GL_GEOMETRY_SHADER)

            if (StageType.FRAGMENT in it)
                shaders += createShaderStage(GLShaderGenerator.createFragmentSource(it, false), GL_FRAGMENT_SHADER)
        }

        if (glIsProgram(shader))
            glDeleteProgram(shader)

        shader = glCreateProgram()

        shaders.forEach {
            glAttachShader(shader, it)
        }

        glLinkProgram(shader)

        shaders.forEach {
            glDetachShader(shader, it)
        }

        if (glGetProgrami(shader, GL_LINK_STATUS) != GL_TRUE)
            Kore.log.fail(this::class, buildString {
                append("Failed to compile shader: ${glGetProgramInfoLog(shader)}\n")

                shaders.forEach {
                    val type = glGetShaderi(it, GL_SHADER_TYPE)

                    append(
                        "${
                            when (type) {
                                GL_VERTEX_SHADER -> "Vertex"
                                GL_GEOMETRY_SHADER -> "Geometry"
                                GL_FRAGMENT_SHADER -> "Fragment"
                                else -> "Unknown"
                            }
                        } source:\n"
                    )

                    glGetShaderSource(it).lines().forEachIndexed { index, line ->
                        append("$index: $line\n")
                    }
                }
            })

        shaders.forEach {
            glDeleteShader(it)
        }

        val activeUniforms = glGetProgrami(shader, GL_ACTIVE_UNIFORMS)
        var textureSlot = 0

        repeat(activeUniforms) { index ->
            val (name: String, count: Int, type: Int) = MemoryStack.stackPush().use {
                val pType = it.mallocInt(1)
                val pSize = it.mallocInt(1)
                val name = glGetActiveUniform(shader, index, 256, pSize, pType)
                Triple(name, pSize.get(0), pType.get(0))
            }

            val uniformType = when (type) {
                GL_INT, GL_INT_VEC2, GL_INT_VEC3, GL_INT_VEC4 -> Uniform.Type.INT
                GL_FLOAT, GL_FLOAT_VEC2, GL_FLOAT_VEC3, GL_FLOAT_VEC4 -> Uniform.Type.FLOAT
                GL_FLOAT_MAT2, GL_FLOAT_MAT3, GL_FLOAT_MAT4 -> Uniform.Type.MATRIX
                GL_BOOL, GL_BOOL_VEC2, GL_BOOL_VEC3, GL_BOOL_VEC4 -> Uniform.Type.BOOLEAN
                GL_SAMPLER_2D -> Uniform.Type.TEXTURE_2D
                GL_SAMPLER_3D -> Uniform.Type.TEXTURE_3D
                GL_SAMPLER_CUBE -> Uniform.Type.TEXTURE_CUBE
                else -> Uniform.Type.UNKNOWN
            }

            val components = when (type) {
                GL_INT, GL_FLOAT, GL_BOOL -> 1
                GL_INT_VEC2, GL_FLOAT_VEC2, GL_BOOL_VEC2 -> 2
                GL_INT_VEC3, GL_FLOAT_VEC3, GL_BOOL_VEC3 -> 3
                GL_INT_VEC4, GL_FLOAT_VEC4, GL_BOOL_VEC4 -> 4
                GL_FLOAT_MAT2 -> 4
                GL_FLOAT_MAT3 -> 9
                GL_FLOAT_MAT4 -> 16
                else -> if (uniformType == Uniform.Type.TEXTURE_2D || uniformType == Uniform.Type.TEXTURE_3D || uniformType == Uniform.Type.TEXTURE_CUBE) 1 else 0
            }

            when (uniformType) {
                Uniform.Type.INT -> uniformMap[name] = GLIntUniform(this, name, components, count)
                Uniform.Type.FLOAT -> uniformMap[name] = GLFloatUniform(this, name, components, count)
                Uniform.Type.MATRIX -> uniformMap[name] = GLMatrixUniform(this, name, components, count)
                Uniform.Type.BOOLEAN -> uniformMap[name] = GLFloatUniform(this, name, components, count)
                Uniform.Type.TEXTURE_2D -> {
                    val uniform = GLTexture2DUniform(this, textureSlot, name, 1, count)
                    uniformMap[name] = uniform
                    applyOnSetPipelineUniforms += uniform
                    textureSlot += count
                }
                Uniform.Type.TEXTURE_3D -> {
                    val uniform = GLTexture3DUniform(this, textureSlot, name, 1, count)
                    uniformMap[name] = uniform
                    applyOnSetPipelineUniforms += uniform
                    textureSlot += count
                }
                Uniform.Type.TEXTURE_CUBE -> {
                    val uniform = GLTextureCubeUniform(this, textureSlot, name, 1, count)
                    uniformMap[name] = uniform
                    applyOnSetPipelineUniforms += uniform
                    textureSlot += count
                }
                else -> Kore.log.error(this::class, "Unknown or unsupported uniform type: $type")
            }
        }

        val activeUniformBlocks = glGetProgrami(shader, GL_ACTIVE_UNIFORM_BLOCKS)
        var binding = 0

        repeat(activeUniformBlocks) {
            val name = glGetActiveUniformBlockName(shader, it)
            val index = glGetUniformBlockIndex(shader, name)
            val size = glGetActiveUniformBlocki(shader, index, GL_UNIFORM_BLOCK_DATA_SIZE)
            glUniformBlockBinding(shader, index, binding)

            val uniform = GL32BufferUniform(this, name, size, binding)
            uniformMap[name] = uniform
            applyOnSetPipelineUniforms += uniform

            binding++
        }
    }
}