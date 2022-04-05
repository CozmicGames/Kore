package com.cozmicgames.graphics.gpu.layout

import com.cozmicgames.memory.Memory
import com.cozmicgames.graphics.gpu.VertexLayout

sealed class VertexComponent<T>(val name: String) {
    abstract class Normal<T>(name: String, val type: VertexLayout.AttributeType, val count: Int, val isNormalized: Boolean) : VertexComponent<T>(name) {
        override fun appendShaderAttributeSource(builder: StringBuilder) {
            builder.append(
                "in ${
                if (isNormalized)
                    when (count) {
                        1 -> "float"
                        2 -> "vec2"
                        3 -> "vec3"
                        4 -> "vec4"
                        else -> throw RuntimeException("Unsupported vertex component format (Type: $type, Count: $count, Normalized: $isNormalized)")
                    }
                else
                    when (type) {
                        VertexLayout.AttributeType.BYTE, VertexLayout.AttributeType.SHORT, VertexLayout.AttributeType.INT -> when (count) {
                            1 -> "int"
                            2 -> "ivec2"
                            3 -> "ivec3"
                            4 -> "ivec4"
                            else -> throw RuntimeException("Unsupported vertex component format (Type: $type, Count: $count, Normalized: $isNormalized)")
                        }
                        VertexLayout.AttributeType.FLOAT -> when (count) {
                            1 -> "float"
                            2 -> "vec2"
                            3 -> "vec3"
                            4 -> "vec4"
                            else -> throw RuntimeException("Unsupported vertex attribute format (Type: $type, Count: $count, Normalized: $isNormalized)")
                        }
                    }
                } $shaderName;\n"
            )
        }

        override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
            builder.append(
                "${if (isNormalized)
                    when (count) {
                        1 -> "float"
                        2 -> "vec2"
                        3 -> "vec3"
                        4 -> "vec4"
                        else -> throw RuntimeException("Unsupported vertex component format (Type: $type, Count: $count, Normalized: $isNormalized)")
                    }
                else
                    when (type) {
                        VertexLayout.AttributeType.BYTE, VertexLayout.AttributeType.SHORT, VertexLayout.AttributeType.INT -> when (count) {
                            1 -> "int"
                            2 -> "ivec2"
                            3 -> "ivec3"
                            4 -> "ivec4"
                            else -> throw RuntimeException("Unsupported vertex component format (Type: $type, Count: $count, Normalized: $isNormalized)")
                        }
                        VertexLayout.AttributeType.FLOAT -> when (count) {
                            1 -> "float"
                            2 -> "vec2"
                            3 -> "vec3"
                            4 -> "vec4"
                            else -> throw RuntimeException("Unsupported vertex attribute format (Type: $type, Count: $count, Normalized: $isNormalized)")
                        }
                    }} $name = $shaderName;\n"
            )
        }
    }

    abstract class Packed<T>(name: String, val packingSize: Int, val packingCount: Int, builder: PackedVertexComponentBuilder) : VertexComponent<T>(name) {
        val offset = builder.usedSize
        val packedComponentName = builder.packedComponentName

        override fun appendShaderAttributeSource(builder: StringBuilder) {}
    }

    val shaderName = "${VertexLayoutBuilder.SHADER_DECLARATION_NAME_PREFIX}$name"

    var layoutAttributeIndex = 0
        internal set

    var index = 0
        internal set

    lateinit var layout: VertexLayout
        internal set

    abstract fun appendShaderAttributeSource(builder: StringBuilder)

    abstract fun appendShaderAttributeToComponentSource(builder: StringBuilder)

    abstract fun writeToMemory(value: T, memory: Memory, offset: Int)

    abstract fun readFromMemory(memory: Memory, offset: Int): T

    abstract fun createVertexComponentObject(): T
}