package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.extract
import com.gratedgames.utils.extensions.insert
import com.gratedgames.utils.maths.Vector3i

class PackedIVec3VertexComponent(name: String, packingSize: Int, builder: PackedVertexComponentBuilder) : VertexComponent.Packed<Vector3i>(name, packingSize, 3, builder) {
    override fun appendShaderAttributeToComponentSource(builder: StringBuilder) {
        builder.append("ivec3 $name = unpackIVec3($packedComponentName, $offset, $packingSize);\n")
    }

    override fun writeToMemory(value: Vector3i, memory: Memory, offset: Int) {
        var packed = memory.getInt(offset)
        packed = packed.insert(value.x, offset, packingSize)
        packed = packed.insert(value.y, offset + packingSize, packingSize)
        packed = packed.insert(value.z, offset + packingSize * 2, packingSize)
        memory.setInt(offset, packed)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Vector3i {
        val packed = memory.getInt(offset)
        val result = Vector3i()
        result.x = packed.extract(offset, packingSize)
        result.y = packed.extract(offset + packingSize, packingSize)
        result.z = packed.extract(offset + packingSize * 2, packingSize)
        return result
    }

    override fun createVertexComponentObject() = Vector3i()
}