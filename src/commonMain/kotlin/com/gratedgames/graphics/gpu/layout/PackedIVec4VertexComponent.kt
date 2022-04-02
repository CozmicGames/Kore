package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.extract
import com.gratedgames.utils.extensions.insert
import com.gratedgames.utils.maths.Vector4i

class PackedIVec4VertexComponent(name: String, packingSize: Int, builder: PackedVertexComponentBuilder) : VertexComponent.Packed<Vector4i>(name, packingSize, 4, builder) {
    override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
        builder.append("ivec4 $name = unpackIVec4($packedComponentName, $offset, $packingSize);\n")
    }

    override fun writeToMemory(value: Vector4i, memory: Memory, offset: Int) {
        var packed = memory.getInt(offset)
        packed = packed.insert(value.x, offset, packingSize)
        packed = packed.insert(value.y, offset + packingSize, packingSize)
        packed = packed.insert(value.z, offset + packingSize * 2, packingSize)
        packed = packed.insert(value.w, offset + packingSize * 3, packingSize)
        memory.setInt(offset, packed)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector4i {
        val packed = memory.getInt(offset)
        val result = Vector4i()
        result.x = packed.extract(offset, packingSize)
        result.y = packed.extract(offset + packingSize, packingSize)
        result.z = packed.extract(offset + packingSize * 2, packingSize)
        result.w = packed.extract(offset + packingSize * 3, packingSize)
        return result
    }

    override fun createVertexComponentObject() = Vector4i()
}