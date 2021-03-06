package com.cozmicgames.graphics.gpu.layout

import com.cozmicgames.Kore
import com.cozmicgames.log
import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.maths.Vector2
import com.cozmicgames.graphics.gpu.VertexLayout

class Vec2VertexComponent(name: String, normalized: Boolean, type: VertexLayout.AttributeType) : VertexComponent.Normal<Vector2>(name, type, 2, normalized) {
    override fun writeToMemory(value: Vector2, memory: Memory, offset: Int) {
        if (!isNormalized) {
            memory.setFloat(offset, value.x)
            memory.setFloat(offset + Memory.SIZEOF_FLOAT, value.y)
        } else when (type) {
            VertexLayout.AttributeType.BYTE -> {
                memory.setByte(offset, (value.x * 0xFF).toInt().toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE, (value.y * 0xFF).toInt().toByte())
            }
            VertexLayout.AttributeType.SHORT -> {
                memory.setShort(offset, (value.x * 0xFFFF).toInt().toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT, (value.y * 0xFFFF).toInt().toShort())
            }
            VertexLayout.AttributeType.INT -> {
                memory.setInt(offset, (value.x * 0xFFFFFFFF).toInt())
                memory.setInt(offset + Memory.SIZEOF_INT, (value.y * 0xFFFFFFFF).toInt())
            }
            else -> Kore.log.error(Vec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
        }
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector2 {
        val result = Vector2()

        if (!isNormalized) {
            result.x = memory.getFloat(offset)
            result.y = memory.getFloat(offset + Memory.SIZEOF_FLOAT)
        } else when (type) {
            VertexLayout.AttributeType.BYTE -> {
                result.x = memory.getByte(offset) / 0xFF.toFloat()
                result.y = memory.getByte(offset + Memory.SIZEOF_BYTE) / 0xFF.toFloat()
            }
            VertexLayout.AttributeType.SHORT -> {
                result.x = memory.getShort(offset) / 0xFFFF.toFloat()
                result.y = memory.getShort(offset + Memory.SIZEOF_SHORT) / 0xFFFF.toFloat()
            }
            VertexLayout.AttributeType.INT -> {
                result.x = memory.getInt(offset) / 0xFFFFFFFF.toFloat()
                result.y = memory.getInt(offset + Memory.SIZEOF_INT) / 0xFFFFFFFF.toFloat()
            }
            else -> Kore.log.error(Vec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
        }

        return result
    }

    override fun createVertexComponentObject() = Vector2()
}