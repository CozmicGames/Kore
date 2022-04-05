package com.cozmicgames.graphics.gpu.layout

import com.cozmicgames.memory.Memory
import com.cozmicgames.graphics.gpu.VertexLayout

class ByteVertexComponent(name: String) : VertexComponent.Normal<Byte>(name, VertexLayout.AttributeType.BYTE, 1, false) {
    override fun writeToMemory(value: Byte, memory: Memory, offset: Int) {
        memory.setByte(offset, value)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Byte {
        return memory.getByte(offset)
    }

    override fun createVertexComponentObject() = 0.toByte()
}