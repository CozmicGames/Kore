package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.extract
import com.gratedgames.utils.extensions.insert

class PackedByteVertexComponent(name: String, packingSize: Int, builder: PackedVertexComponentBuilder) : VertexComponent.Packed<Byte>(name, packingSize, 1, builder) {
    override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
        builder.append("int $name = unpackInt($packedComponentName, $offset, $packingSize);\n")
    }

    override fun writeToMemory(value: Byte, memory: Memory, offset: Int) {
        val packed = memory.getInt(offset)
        memory.setInt(offset, packed.insert(value.toInt(), offset, packingSize))
    }

    override fun readFromMemory(memory: Memory, offset: Int): Byte {
        return memory.getInt(offset).extract(offset, packingSize).toByte()
    }

    override fun createVertexComponentObject() = 0.toByte()
}