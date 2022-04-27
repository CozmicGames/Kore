package com.cozmicgames.graphics.opengl.gl43

import com.cozmicgames.Kore
import com.cozmicgames.graphics.gpu.Uniform
import com.cozmicgames.graphics.gpu.pipeline.StageType
import com.cozmicgames.graphics.gpu.pipeline.contains
import com.cozmicgames.graphics.opengl.*
import com.cozmicgames.log
import com.cozmicgames.utils.maths.Vector3i
import org.lwjgl.opengl.GL43C.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush

class GL43Pipeline(override val type: Type) : GLPipeline() {
    override var shader = 0

    override val workgroupSizes by lazy {
        if (type == Type.GRAPHICS)
            Vector3i.ZERO
        else {
            val sizes = Vector3i()
            stackPush().use {
                val pSizes = it.callocInt(3)
                glGetProgramiv(shader, GL_COMPUTE_WORK_GROUP_SIZE, pSizes)
                sizes.x = pSizes[0]
                sizes.y = pSizes[1]
                sizes.z = pSizes[2]
            }
            sizes
        }
    }

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
                shaders += createShaderStage(GLShaderGenerator.createVertexSource(it, vertexLayout, true), GL_VERTEX_SHADER)

            if (StageType.GEOMETRY in it)
                shaders += createShaderStage(GLShaderGenerator.createGeometrySource(it, true), GL_GEOMETRY_SHADER)

            if (StageType.FRAGMENT in it)
                shaders += createShaderStage(GLShaderGenerator.createFragmentSource(it, true), GL_FRAGMENT_SHADER)

            if (StageType.COMPUTE in it)
                shaders += createShaderStage(GLShaderGenerator.createComputeSource(it, true), GL_COMPUTE_SHADER)
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
            val (name: String, count: Int, type: Int) = stackPush().use {
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
                GL_IMAGE_2D -> Uniform.Type.IMAGE_2D
                GL_IMAGE_3D -> Uniform.Type.IMAGE_3D
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
                else -> if (uniformType == Uniform.Type.TEXTURE_2D || uniformType == Uniform.Type.TEXTURE_3D || uniformType == Uniform.Type.TEXTURE_CUBE || uniformType == Uniform.Type.BUFFER || uniformType == Uniform.Type.IMAGE_2D || uniformType == Uniform.Type.IMAGE_3D) 1 else 0
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
                Uniform.Type.IMAGE_2D -> {
                    require(count == 1)
                    val uniform = GLImage2DUniform(this, textureSlot, name)
                    uniformMap[name] = uniform
                    applyOnSetPipelineUniforms += uniform
                    textureSlot += count
                }
                Uniform.Type.IMAGE_3D -> {
                    require(count == 1)
                    val uniform = GLImage2DUniform(this, textureSlot, name)
                    uniformMap[name] = uniform
                    applyOnSetPipelineUniforms += uniform
                    textureSlot += count
                }
                else -> Kore.log.error(this::class, "Unknown uniform type: $type")
            }
        }

        val activeBuffers = glGetProgramInterfacei(shader, GL_SHADER_STORAGE_BLOCK, GL_ACTIVE_RESOURCES)
        var binding = 0

        repeat(activeBuffers) {
            val name = glGetProgramResourceName(shader, GL_SHADER_STORAGE_BLOCK, it)
            val index = glGetProgramResourceIndex(shader, GL_SHADER_STORAGE_BLOCK, name)
            val size = stackPush().use {
                val pProps = it.ints(GL_BUFFER_DATA_SIZE)
                val pLengths = it.callocInt(1)
                val pParams = it.callocInt(1)

                glGetProgramResourceiv(shader, GL_SHADER_STORAGE_BLOCK, index, pProps, pLengths, pParams)
                pParams.get(0)
            }

            glShaderStorageBlockBinding(shader, index, binding)

            val uniform = GL43BufferUniform(this, name, size, binding)
            uniformMap[name] = uniform
            applyOnSetPipelineUniforms += uniform

            binding++
        }
    }
}