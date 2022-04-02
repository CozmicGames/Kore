package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.extract
import com.gratedgames.utils.extensions.insert

class PackedIntVertexComponent(name: String, packingSize: Int, builder: PackedVertexComponentBuilder) : VertexComponent.Packed<Int>(name, packingSize, 1, builder) {
    override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
        builder.append("int $name = unpackInt($packedComponentName, $offset, $packingSize);\n")
    }

    override fun writeToMemory(value: Int, memory: Memory, offset: Int) {
        val packed = memory.getInt(offset)
        memory.setInt(offset, packed.insert(value, offset, packingSize))
    }

    override fun readFromMemory(memory: Memory, offset: Int): Int {
        return memory.getInt(offset).extract(offset, packingSize)
    }

    override fun createVertexComponentObject() = 0
}