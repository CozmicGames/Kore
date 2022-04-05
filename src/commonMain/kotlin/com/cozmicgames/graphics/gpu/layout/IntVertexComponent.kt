package com.cozmicgames.graphics.gpu.layout

import com.cozmicgames.graphics.gpu.VertexLayout
import com.cozmicgames.memory.Memory

class IntVertexComponent(name: String) : VertexComponent.Normal<Int>(name, VertexLayout.AttributeType.INT, 1, false) {
    override fun writeToMemory(value: Int, memory: Memory, offset: Int) {
        memory.setInt(offset, value)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Int {
        return memory.getInt(offset)
    }

    override fun createVertexComponentObject() = 0
}