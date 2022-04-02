package com.gratedgames.graphics.gpu.layout

import com.gratedgames.Kore
import com.gratedgames.graphics.gpu.VertexLayout
import com.gratedgames.log
import com.gratedgames.memory.Memory
import com.gratedgames.utils.maths.Vector2i

class IVec2VertexComponent(name: String, type: VertexLayout.AttributeType) : VertexComponent.Normal<Vector2i>(name, type, 2, false) {
    override fun writeToMemory(value: Vector2i, memory: Memory, offset: Int) {
        when (type) {
            VertexLayout.AttributeType.BYTE -> {
                memory.setByte(offset, value.x.toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE, value.y.toByte())
            }
            VertexLayout.AttributeType.SHORT -> {
                memory.setShort(offset, value.x.toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT, value.y.toShort())
            }
            VertexLayout.AttributeType.INT -> {
                memory.setInt(offset, value.x)
                memory.setInt(offset + Memory.SIZEOF_INT, value.y)
            }
            else -> Kore.log.error(IVec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type)")
        }
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector2i {
        val result = Vector2i()

        when (type) {
            VertexLayout.AttributeType.BYTE -> {
                result.x = memory.getByte(offset).toInt()
                result.y = memory.getByte(offset + Memory.SIZEOF_BYTE).toInt()
            }
            VertexLayout.AttributeType.SHORT -> {
                result.x = memory.getShort(offset).toInt()
                result.y = memory.getShort(offset + Memory.SIZEOF_SHORT).toInt()
            }
            VertexLayout.AttributeType.INT -> {
                result.x = memory.getInt(offset)
                result.y = memory.getInt(offset + Memory.SIZEOF_INT)
            }
            else -> Kore.log.error(IVec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type)")
        }

        return result
    }

    override fun createVertexComponentObject() = Vector2i()
}