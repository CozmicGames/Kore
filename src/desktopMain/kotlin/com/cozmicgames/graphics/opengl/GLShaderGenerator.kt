package com.cozmicgames.graphics.opengl

import com.cozmicgames.graphics.gpu.VertexLayout
import com.cozmicgames.graphics.gpu.pipeline.*
import com.cozmicgames.utils.extensions.removeBlankLines
import com.cozmicgames.utils.extensions.removeComments
import org.lwjgl.opengl.GL
import java.lang.Exception
import java.lang.StringBuilder

object GLShaderGenerator {
    private fun getGLSLVersionString(): String {
        val caps = GL.getCapabilities()
        return when {
            caps.OpenGL46 -> "460"
            caps.OpenGL45 -> "450"
            caps.OpenGL44 -> "440"
            caps.OpenGL43 -> "430"
            caps.OpenGL42 -> "420"
            caps.OpenGL41 -> "410"
            caps.OpenGL40 -> "400"
            caps.OpenGL33 -> "330"
            else -> "150"
        }
    }

    private fun appendUniforms(builder: StringBuilder, source: ProgramSource, useShaderStorageBuffers: Boolean) {
        source.uniforms.forEach {
            when (it.type) {
                UniformDefinition.Type.BUFFER -> {
                    if (useShaderStorageBuffers)
                        builder.appendLine("layout(std430) buffer ${it.name} {")
                    else
                        builder.appendLine("layout(std140) uniform ${it.name} {")
                    (it as BufferUniformDefinition).content.forEach {
                        if (it is BufferUniformDefinition.ArrayProperty)
                            builder.appendLine("${it.type} ${it.name}[${it.size}];")
                        else
                            builder.appendLine("${it.type} ${it.name};")
                    }
                    builder.appendLine("};")
                }
                UniformDefinition.Type.IMAGE_2D -> builder.appendLine("layout(${(it as Image2DUniformDefinition).format.toGLSLType()}) uniform ${it.format.toGLSLImagePrefix()}image2D ${it.name};")
                UniformDefinition.Type.IMAGE_3D -> builder.appendLine("layout(${(it as Image3DUniformDefinition).format.toGLSLType()}) uniform ${it.format.toGLSLImagePrefix()}image3D ${it.name};")
                else -> {
                    val glslType = when (it.type) {
                        UniformDefinition.Type.BOOLEAN -> "bool"
                        UniformDefinition.Type.FLOAT -> "float"
                        UniformDefinition.Type.INT -> "int"
                        UniformDefinition.Type.IVEC2 -> "ivec2"
                        UniformDefinition.Type.IVEC3 -> "ivec3"
                        UniformDefinition.Type.IVEC4 -> "ivec4"
                        UniformDefinition.Type.MATRIX -> "mat4"
                        UniformDefinition.Type.TEXTURE_2D -> "sampler2D"
                        UniformDefinition.Type.TEXTURE_3D -> "sampler3D"
                        UniformDefinition.Type.TEXTURE_CUBE -> "samplerCube"
                        UniformDefinition.Type.VEC2 -> "vec2"
                        UniformDefinition.Type.VEC3 -> "vec3"
                        UniformDefinition.Type.VEC4 -> "vec4"
                        else -> throw Exception()
                    }

                    if (it.size > 1)
                        builder.appendLine("uniform $glslType ${it.name}[${it.size}];")
                    else
                        builder.appendLine("uniform $glslType ${it.name};")
                }
            }
        }
    }

    private fun appendTypes(builder: StringBuilder, source: ProgramSource) {
        source.types.forEach {
            builder.appendLine("struct ${it.name} {")
            it.content.forEach {
                if (it is TypeDefinition.ArrayProperty)
                    builder.appendLine("${it.type} ${it.name}[${it.size}];")
                else
                    builder.appendLine("${it.type} ${it.name};")
            }
            builder.appendLine("};")
        }
    }

    fun createVertexSource(source: ProgramSource, layout: VertexLayout?, useShaderStorageBuffers: Boolean): String {
        val lines = (source[StageType.VERTEX] as VertexSource).preprocessedSource.removeComments().removeBlankLines().lines()

        return buildString {
            appendLine("#version ${getGLSLVersionString()}")
            source.defines.forEach {
                appendLine("#define $it")
            }
            layout?.let {
                appendLine(it.shaderAttributeSource)
            }
            appendTypes(this, source)
            appendUniforms(this, source, useShaderStorageBuffers)
            lines.forEach {
                val line = it.trim()
                if (line.isNotBlank() && line.isNotEmpty()) {
                    val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }
                    if (parts.size >= 2 && parts[0] == "void" && parts[1].startsWith("main")) {
                        appendLine("void main() {")
                        layout?.let {
                            appendLine(it.shaderAttributeToComponentSource)
                        }
                    } else
                        appendLine(it)
                }
            }
        }
    }

    fun createGeometrySource(source: ProgramSource, useShaderStorageBuffers: Boolean): String {
        val lines = (source[StageType.GEOMETRY] as GeometrySource).preprocessedSource.removeComments().removeBlankLines().lines()

        return buildString {
            appendLine("#version ${getGLSLVersionString()}")
            source.defines.forEach {
                appendLine("#define $it")
            }
            appendTypes(this, source)
            appendUniforms(this, source, useShaderStorageBuffers)
            lines.forEach(::appendLine)
        }
    }

    fun createFragmentSource(source: ProgramSource, useShaderStorageBuffers: Boolean): String {
        val lines = (source[StageType.FRAGMENT] as FragmentSource).preprocessedSource.removeComments().removeBlankLines().lines()

        return buildString {
            appendLine("#version ${getGLSLVersionString()}")
            source.defines.forEach {
                appendLine("#define $it")
            }
            appendTypes(this, source)
            appendUniforms(this, source, useShaderStorageBuffers)
            lines.forEach(::appendLine)
        }
    }

    fun createComputeSource(source: ProgramSource, useShaderStorageBuffers: Boolean): String {
        val lines = (source[StageType.COMPUTE] as ComputeSource).preprocessedSource.removeComments().removeBlankLines().lines()

        return buildString {
            appendLine("#version ${getGLSLVersionString()}")
            appendLine("#define NUM_THREADS(_x, _y, _z) layout (local_size_x = _x, local_size_y = _y, local_size_z = _z) in;")
            source.defines.forEach {
                appendLine("#define $it")
            }
            appendTypes(this, source)
            appendUniforms(this, source, useShaderStorageBuffers)
            lines.forEach(::appendLine)
        }
    }
}