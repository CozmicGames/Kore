package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.graphics.gpu.VertexLayout

class ByteVertexComponent(name: String) : VertexComponent.Normal<Byte>(name, VertexLayout.AttributeType.BYTE, 1, false) {
    override fun writeToMemory(value: Byte, memory: Memory, offset: Int) {
        memory.setByte(offset, value)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Byte {
        return memory.getByte(offset)
    }

    override fun createVertexComponentObject() = 0.toByte()
}