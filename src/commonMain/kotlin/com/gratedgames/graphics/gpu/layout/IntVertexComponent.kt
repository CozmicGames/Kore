package com.gratedgames.graphics.gpu.layout

import com.gratedgames.graphics.gpu.VertexLayout
import com.gratedgames.memory.Memory

class IntVertexComponent(name: String) : VertexComponent.Normal<Int>(name, VertexLayout.AttributeType.INT, 1, false) {
    override fun writeToMemory(value: Int, memory: Memory, offset: Int) {
        memory.setInt(offset, value)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Int {
        return memory.getInt(offset)
    }

    override fun createVertexComponentObject() = 0
}