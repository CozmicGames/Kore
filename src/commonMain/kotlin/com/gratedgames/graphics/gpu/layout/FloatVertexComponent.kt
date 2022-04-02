package com.gratedgames.graphics.gpu.layout

import com.gratedgames.Kore
import com.gratedgames.log
import com.gratedgames.memory.Memory
import com.gratedgames.graphics.gpu.VertexLayout

class FloatVertexComponent(name: String, normalized: Boolean, type: VertexLayout.AttributeType) : VertexComponent.Normal<Float>(name, type, 1, normalized) {
    override fun writeToMemory(value: Float, memory: Memory, offset: Int) {
        if (!isNormalized)
            memory.setFloat(offset, value)
        else when (type) {
            VertexLayout.AttributeType.BYTE -> memory.setByte(offset, (value * 0xFF).toInt().toByte())
            VertexLayout.AttributeType.SHORT -> memory.setShort(offset, (value * 0xFFFF).toInt().toShort())
            VertexLayout.AttributeType.INT -> memory.setInt(offset, (value * 0xFFFFFFFF).toInt())
            else -> Kore.log.error(FloatVertexComponent::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
        }
    }

    override fun readFromMemory(memory: Memory, offset: Int): Float {
        return if (!isNormalized)
            memory.getFloat(offset)
        else when (type) {
            VertexLayout.AttributeType.BYTE -> memory.getByte(offset) / 0xFF.toFloat()
            VertexLayout.AttributeType.SHORT -> memory.getShort(offset) / 0xFFFF.toFloat()
            VertexLayout.AttributeType.INT -> memory.getInt(offset) / 0xFFFFFFFF.toFloat()
            else -> {
                Kore.log.fail(this::class, "Unsupported vertex component configuration (Type: $type, Normalized: $isNormalized)")
                throw RuntimeException()
            }
        }
    }

    override fun createVertexComponentObject() = 0.0f
}