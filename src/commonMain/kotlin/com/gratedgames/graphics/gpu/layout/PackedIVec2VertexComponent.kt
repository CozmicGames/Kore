package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.extract
import com.gratedgames.utils.extensions.insert
import com.gratedgames.utils.maths.Vector2i

class PackedIVec2VertexComponent(name: String, packingSize: Int, builder: PackedVertexComponentBuilder) : VertexComponent.Packed<Vector2i>(name, packingSize, 2, builder) {
    override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
        builder.append("ivec2 $name = unpackIVec2($packedComponentName, $offset, $packingSize);\n")
    }

    override fun writeToMemory(value: Vector2i, memory: Memory, offset: Int) {
        var packed = memory.getInt(offset)
        packed = packed.insert(value.x, offset, packingSize)
        packed = packed.insert(value.y, offset + packingSize, packingSize)
        memory.setInt(offset, packed)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector2i {
        val packed = memory.getInt(offset)
        val result = Vector2i()
        result.x = packed.extract(offset, packingSize)
        result.y = packed.extract(offset + packingSize, packingSize)
        return result
    }

    override fun createVertexComponentObject() = Vector2i()
}