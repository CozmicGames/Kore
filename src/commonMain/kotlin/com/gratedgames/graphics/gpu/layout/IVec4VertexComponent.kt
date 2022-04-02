package com.gratedgames.graphics.gpu.layout

import com.gratedgames.Kore
import com.gratedgames.graphics.gpu.VertexLayout
import com.gratedgames.log
import com.gratedgames.memory.Memory
import com.gratedgames.utils.maths.Vector4i


class IVec4VertexComponent(name: String, type: VertexLayout.AttributeType) : VertexComponent.Normal<Vector4i>(name, type, 3, false) {
    override fun writeToMemory(value: Vector4i, memory: Memory, offset: Int) {
        when (type) {
            VertexLayout.AttributeType.BYTE -> {
                memory.setByte(offset, value.x.toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE, value.y.toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE * 2, value.z.toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE * 3, value.z.toByte())
            }
            VertexLayout.AttributeType.SHORT -> {
                memory.setShort(offset, value.x.toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT, value.y.toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT * 2, value.z.toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT * 3, value.z.toShort())
            }
            VertexLayout.AttributeType.INT -> {
                memory.setInt(offset, value.x)
                memory.setInt(offset + Memory.SIZEOF_INT, value.y)
                memory.setInt(offset + Memory.SIZEOF_INT * 2, value.z)
                memory.setInt(offset + Memory.SIZEOF_INT * 3, value.z)
            }
            else -> Kore.log.error(IVec4VertexComponent::class, "Unsupported vertex component configuration (Type: $type)")
        }
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector4i {
        val result = Vector4i()

        when (type) {
            VertexLayout.AttributeType.BYTE -> {
                result.x = memory.getByte(offset).toInt()
                result.y = memory.getByte(offset + Memory.SIZEOF_BYTE).toInt()
                result.z = memory.getByte(offset + Memory.SIZEOF_BYTE * 2).toInt()
                result.w = memory.getByte(offset + Memory.SIZEOF_BYTE * 3).toInt()
            }
            VertexLayout.AttributeType.SHORT -> {
                result.x = memory.getShort(offset).toInt()
                result.y = memory.getShort(offset + Memory.SIZEOF_SHORT).toInt()
                result.z = memory.getShort(offset + Memory.SIZEOF_SHORT * 2).toInt()
                result.w = memory.getShort(offset + Memory.SIZEOF_SHORT * 3).toInt()
            }
            VertexLayout.AttributeType.INT -> {
                result.x = memory.getInt(offset)
                result.y = memory.getInt(offset + Memory.SIZEOF_INT)
                result.z = memory.getInt(offset + Memory.SIZEOF_INT * 2)
                result.w = memory.getInt(offset + Memory.SIZEOF_INT * 3)
            }
            else -> Kore.log.error(IVec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type)")
        }

        return result
    }

    override fun createVertexComponentObject() = Vector4i()
}