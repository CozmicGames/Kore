package com.gratedgames.graphics.gpu.layout

import com.gratedgames.Kore
import com.gratedgames.log

class PackedVertexComponentBuilder(val layoutBuilder: VertexLayoutBuilder, val packedComponentName: String) {
    var usedSize = 0
        set(value) {
            if (value > 32)
                Kore.log.fail(this::class, "Only 32 bits are available for vertex packing")
            field = value
        }

    val components = arrayListOf<VertexComponent.Packed<*>>()

    fun packedByte(name: String, size: Int) = addComponent(PackedByteVertexComponent(name, size, this))

    fun packedShort(name: String, size: Int) = addComponent(PackedShortVertexComponent(name, size, this))

    fun packedInt(name: String, size: Int) = addComponent(PackedIntVertexComponent(name, size, this))

    fun packedIVec2(name: String, size: Int) = addComponent(PackedIVec2VertexComponent(name, size, this))

    fun packedIVec3(name: String, size: Int) = addComponent(PackedIVec3VertexComponent(name, size, this))

    fun packedIVec4(name: String, size: Int) = addComponent(PackedIVec4VertexComponent(name, size, this))

    fun addComponent(component: VertexComponent.Packed<*>) {
        usedSize += component.packingCount * component.packingSize
        components += component
        layoutBuilder.addComponent(component)
    }
}