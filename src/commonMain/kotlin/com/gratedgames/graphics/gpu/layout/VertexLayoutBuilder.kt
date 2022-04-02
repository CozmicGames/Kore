package com.gratedgames.graphics.gpu.layout

import com.gratedgames.memory.Memory
import com.gratedgames.utils.Color
import com.gratedgames.graphics.gpu.VertexLayout

class VertexLayoutBuilder {
    companion object {
        const val SHADER_DECLARATION_NAME_PREFIX = "_VertexAttribute_"
    }

    private val shaderAttributeSourceBuilder = StringBuilder()
    private val shaderAttributeToComponentSourceBuilder = StringBuilder()

    internal val components = arrayListOf<VertexComponent<*>>()
    internal val attributes = arrayListOf<VertexLayout.Attribute>()

    private var numPackedAttributes = 0
    private var isPacking = false

    private var currentComponentIndex = 0

    private fun generatePackedAttributeName() = "${SHADER_DECLARATION_NAME_PREFIX}PackedAttributeData_${numPackedAttributes++}"

    fun packed(name: String = generatePackedAttributeName(), block: PackedVertexComponentBuilder.() -> Unit) {
        require(!isPacking)
        isPacking = true

        val builder = PackedVertexComponentBuilder(this, name)
        block(builder)

        attributes += VertexLayout.Attribute(VertexLayout.AttributeType.INT, 1, false, true)

        shaderAttributeSourceBuilder.append("in int $name;\n")

        builder.components.forEach {
            it.layoutAttributeIndex = attributes.lastIndex
        }

        isPacking = false
    }

    fun byte(name: String) = addComponent(ByteVertexComponent(name))

    fun short(name: String) = addComponent(ShortVertexComponent(name))

    fun int(name: String) = addComponent(IntVertexComponent(name))

    fun ivec2(name: String, type: VertexLayout.AttributeType = VertexLayout.AttributeType.INT) = addComponent(IVec2VertexComponent(name, type))

    fun ivec3(name: String, type: VertexLayout.AttributeType = VertexLayout.AttributeType.INT) = addComponent(IVec3VertexComponent(name, type))

    fun ivec4(name: String, type: VertexLayout.AttributeType = VertexLayout.AttributeType.INT) = addComponent(IVec4VertexComponent(name, type))

    fun float(name: String, isNormalized: Boolean = false, type: VertexLayout.AttributeType = VertexLayout.AttributeType.FLOAT) = addComponent(FloatVertexComponent(name, isNormalized, type))

    fun vec2(name: String, isNormalized: Boolean = false, type: VertexLayout.AttributeType = VertexLayout.AttributeType.FLOAT) = addComponent(Vec2VertexComponent(name, isNormalized, type))

    fun vec3(name: String, isNormalized: Boolean = false, type: VertexLayout.AttributeType = VertexLayout.AttributeType.FLOAT) = addComponent(Vec3VertexComponent(name, isNormalized, type))

    fun vec4(name: String, isNormalized: Boolean = false, type: VertexLayout.AttributeType = VertexLayout.AttributeType.FLOAT) = addComponent(Vec4VertexComponent(name, isNormalized, type))

    fun <T> addComponent(component: VertexComponent<T>) {
        components += component

        component.appendShaderAttributeSource(shaderAttributeSourceBuilder)
        component.appendShaderAttributeToComponentSource(shaderAttributeToComponentSourceBuilder)

        component.index = currentComponentIndex++

        if (component is VertexComponent.Normal) {
            require(!isPacking)

            val type = when (component.type) {
                VertexLayout.AttributeType.BYTE -> VertexLayout.AttributeType.BYTE
                VertexLayout.AttributeType.SHORT -> VertexLayout.AttributeType.SHORT
                VertexLayout.AttributeType.INT -> VertexLayout.AttributeType.INT
                VertexLayout.AttributeType.FLOAT -> VertexLayout.AttributeType.FLOAT
            }

            val asInt = !component.isNormalized && (component.type == VertexLayout.AttributeType.BYTE || component.type == VertexLayout.AttributeType.SHORT || component.type == VertexLayout.AttributeType.INT)

            attributes += VertexLayout.Attribute(type, component.count, component.isNormalized, asInt)

            component.layoutAttributeIndex = attributes.lastIndex
        }
    }

    internal fun getShaderAttributeSource() = shaderAttributeSourceBuilder.toString()

    internal fun getShaderAttributeToComponentSource() = shaderAttributeToComponentSourceBuilder.toString()
}

fun VertexLayoutBuilder.color(name: String) = addComponent(object : VertexComponent.Normal<Color>(name, VertexLayout.AttributeType.BYTE, 4, true) {
    override fun writeToMemory(value: Color, memory: Memory, offset: Int) {
        memory.setInt(offset, value.bits)
    }

    override fun readFromMemory(memory: Memory, offset: Int): Color {
        return Color.fromRGBA(memory.getInt(offset))
    }

    override fun createVertexComponentObject() = Color()
})