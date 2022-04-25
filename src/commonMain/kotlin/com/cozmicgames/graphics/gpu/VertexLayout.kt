package com.cozmicgames.graphics.gpu

import com.cozmicgames.graphics.gpu.layout.VertexComponent
import com.cozmicgames.graphics.gpu.layout.VertexLayoutBuilder
import com.cozmicgames.memory.Memory

class VertexLayout(block: VertexLayoutBuilder.() -> Unit) : Iterable<String> {
    enum class AttributeType(val size: Int) {
        BYTE(Memory.SIZEOF_BYTE),
        SHORT(Memory.SIZEOF_SHORT),
        INT(Memory.SIZEOF_INT),
        FLOAT(Memory.SIZEOF_FLOAT)
    }

    data class Attribute(val type: AttributeType, val count: Int, val isNormalized: Boolean, val asInt: Boolean) {
        val size get() = count * type.size

        lateinit var layout: VertexLayout
            internal set
    }

    private val components: Array<VertexComponent<*>>
    private val componentIndices = hashMapOf<String, Int>()

    val attributes: Array<Attribute>
    val stride: Int
    val offsets: IntArray
    val indices: IntArray

    val layoutAttributeCount get() = attributes.size
    val shaderAttributeSource: String
    val shaderAttributeToComponentSource: String

    init {
        val builder = VertexLayoutBuilder()
        block(builder)

        components = builder.components.toTypedArray()
        attributes = builder.attributes.toTypedArray()

        components.forEach {
            it.layout = this
        }

        attributes.forEach {
            it.layout = this
        }

        components.forEachIndexed { index, component ->
            componentIndices[component.name] = index
        }

        offsets = IntArray(layoutAttributeCount)
        indices = IntArray(layoutAttributeCount)

        var currentStride = 0
        attributes.forEachIndexed { index, attribute ->
            offsets[index] = currentStride
            currentStride += attribute.size
            indices[index] = index
        }

        stride = currentStride

        shaderAttributeSource = builder.getShaderAttributeSource()
        shaderAttributeToComponentSource = builder.getShaderAttributeToComponentSource()
    }

    fun getVertexAttributeIndex(name: String) = componentIndices[name]

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): VertexComponent<T>? {
        val index = getVertexAttributeIndex(name) ?: return null
        return this[index] as? VertexComponent<T>?
    }

    operator fun get(index: Int) = components[index]

    override fun iterator() = componentIndices.keys.iterator() as Iterator<String>
}
