package com.gratedgames.graphics.gpu.layout

import com.gratedgames.Kore
import com.gratedgames.log
import com.gratedgames.memory.Memory
import com.gratedgames.utils.maths.Vector3
import com.gratedgames.graphics.gpu.VertexLayout

class Vec3VertexComponent(name: String, normalized: Boolean, type: VertexLayout.AttributeType) : VertexComponent.Normal<Vector3>(name, type, 3, normalized) {
    override fun writeToMemory(value: Vector3, memory: Memory, offset: Int) {
        if (!isNormalized) {
            memory.setFloat(offset, value.x)
            memory.setFloat(offset + Memory.SIZEOF_FLOAT, value.y)
            memory.setFloat(offset + Memory.SIZEOF_FLOAT * 2, value.z)
        } else when (type) {
            VertexLayout.AttributeType.BYTE -> {
                memory.setByte(offset, (value.x * 0xFF).toInt().toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE, (value.y * 0xFF).toInt().toByte())
                memory.setByte(offset + Memory.SIZEOF_BYTE * 2, (value.z * 0xFF).toInt().toByte())
            }
            VertexLayout.AttributeType.SHORT -> {
                memory.setShort(offset, (value.x * 0xFFFF).toInt().toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT, (value.y * 0xFFFF).toInt().toShort())
                memory.setShort(offset + Memory.SIZEOF_SHORT * 2, (value.z * 0xFFFF).toInt().toShort())
            }
            VertexLayout.AttributeType.INT -> {
                memory.setInt(offset, (value.x * 0xFFFFFFFF).toInt())
                memory.setInt(offset + Memory.SIZEOF_INT, (value.y * 0xFFFFFFFF).toInt())
                memory.setInt(offset + Memory.SIZEOF_INT * 2, (value.z * 0xFFFFFFFF).toInt())
            }
            else -> Kore.log.error(Vec3VertexComponent::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
        }
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector3 {
        val result = Vector3()

        if (!isNormalized) {
            result.x = memory.getFloat(offset)
            result.y = memory.getFloat(offset + Memory.SIZEOF_FLOAT)
            result.z = memory.getFloat(offset + Memory.SIZEOF_FLOAT * 2)
        } else when (type) {
            VertexLayout.AttributeType.BYTE -> {
                result.x = memory.getByte(offset) / 0xFF.toFloat()
                result.y = memory.getByte(offset + Memory.SIZEOF_BYTE) / 0xFF.toFloat()
                result.z = memory.getByte(offset + Memory.SIZEOF_BYTE * 2) / 0xFF.toFloat()
            }
            VertexLayout.AttributeType.SHORT -> {
                result.x = memory.getShort(offset) / 0xFFFF.toFloat()
                result.y = memory.getShort(offset + Memory.SIZEOF_SHORT) / 0xFFFF.toFloat()
                result.z = memory.getShort(offset + Memory.SIZEOF_SHORT * 2) / 0xFFFF.toFloat()
            }
            VertexLayout.AttributeType.INT -> {
                result.x = memory.getInt(offset) / 0xFFFFFFFF.toFloat()
                result.y = memory.getInt(offset + Memory.SIZEOF_INT) / 0xFFFFFFFF.toFloat()
                result.z = memory.getInt(offset + Memory.SIZEOF_INT * 2) / 0xFFFFFFFF.toFloat()
            }
            else -> Kore.log.error(Vec2VertexComponent::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
        }

        return result
    }

    override fun createVertexComponentObject() = Vector3()
}