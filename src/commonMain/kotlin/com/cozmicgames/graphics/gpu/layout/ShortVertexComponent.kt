package com.cozmicgames.graphics.gpu.layout

import com.cozmicgames.memory.Memory
import com.cozmicgames.graphics.gpu.VertexLayout

class ShortVertexComponent(name: String) : VertexComponent.Normal<Short>(name, VertexLayout.AttributeType.SHORT, 1, false) {
    override fun writeToMemory(value: Short, memory: Memory, offset: Int) {
        memory.setShort(offset, value)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Short {
        return memory.getShort(offset)
    }

    override fun createVertexComponentObject() = 0.toShort()
}