package com.gratedgames.graphics.gpu.pipeline

class TypeDefinition(val name: String) {
    open class Property(val type: String, val name: String, sizes: TypeSizes) {
        val dataSize = requireNotNull(sizes.getSize(type))
    }

    class ArrayProperty(type: String, name: String, val size: Int, sizes: TypeSizes) : Property(type, name, sizes)

    val content = arrayListOf<Property>()

    val dataSize
        get() = content.sumOf {
            if (it is ArrayProperty)
                it.dataSize * it.size
            else
                it.dataSize
        }
}