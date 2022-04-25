package com.cozmicgames.graphics.gpu

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.*

abstract class Uniform<T>(val name: String, val type: Type, val numComponents: Int, val count: Int) {
    enum class Type {
        UNKNOWN,
        INT,
        FLOAT,
        MATRIX,
        BOOLEAN,
        TEXTURE_2D,
        TEXTURE_3D,
        TEXTURE_CUBE,
        BUFFER,
        IMAGE_2D,
        IMAGE_3D
    }

    abstract fun update(block: (Array<T>) -> Unit)
}

fun <T : Any?> Uniform<T>.update(value: T, index: Int = 0) = update { it[index] = value }

class IntUniform(val baseUniform: Uniform<Int>) : Uniform<Int>(baseUniform.name, baseUniform.type, baseUniform.numComponents, baseUniform.count) {
    private val values = Array(numComponents * count) { 0 }

    override fun update(block: (Array<Int>) -> Unit) {
        block(values)

        baseUniform.update {
            values.copyInto(it)
        }
    }
}

fun IntUniform.update(vararg values: Int) = update {
    repeat(values.size) { index ->
        it[index] = values[index]
    }
}

fun IntUniform.update(vararg vectors: Vector2i) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 2] = vector.x
        it[index * 2 + 1] = vector.y
    }
}

fun IntUniform.update(vararg vectors: Vector3i) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 3] = vector.x
        it[index * 3 + 1] = vector.y
        it[index * 3 + 2] = vector.z
    }
}

fun IntUniform.update(vararg vectors: Vector4i) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 4] = vector.x
        it[index * 4 + 1] = vector.y
        it[index * 4 + 2] = vector.z
        it[index * 4 + 3] = vector.w
    }
}

class FloatUniform(val baseUniform: Uniform<Float>) : Uniform<Float>(baseUniform.name, baseUniform.type, baseUniform.numComponents, baseUniform.count) {
    private val values = Array(numComponents * count) { 0.0f }

    override fun update(block: (Array<Float>) -> Unit) {
        block(values)

        baseUniform.update {
            values.copyInto(it)
        }
    }
}

fun FloatUniform.update(vararg values: Float) = update {
    repeat(values.size) { index ->
        it[index] = values[index]
    }
}

fun FloatUniform.update(vararg vectors: Vector2) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 2] = vector.x
        it[index * 2 + 1] = vector.y
    }
}

fun FloatUniform.update(vararg vectors: Vector3) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 3] = vector.x
        it[index * 3 + 1] = vector.y
        it[index * 3 + 2] = vector.z
    }
}

fun FloatUniform.update(vararg vectors: Vector4) = update {
    vectors.forEachIndexed { index, vector ->
        it[index * 4] = vector.x
        it[index * 4 + 1] = vector.y
        it[index * 4 + 2] = vector.z
        it[index * 4 + 3] = vector.w
    }
}

fun FloatUniform.update(vararg colors: Color) = update {
    colors.forEachIndexed { index, color ->
        it[index * 4] = color.r
        it[index * 4 + 1] = color.g
        it[index * 4 + 2] = color.b
        it[index * 4 + 3] = color.a
    }
}

class MatrixUniform(val baseUniform: Uniform<Float>) : Uniform<Matrix4x4>(baseUniform.name, baseUniform.type, 1, baseUniform.count) {
    private val values = Array(numComponents * count) { Matrix4x4() }

    override fun update(block: (Array<Matrix4x4>) -> Unit) {
        block(values)

        baseUniform.update {
            values.forEachIndexed { index, matrix ->
                matrix.data.copyInto(it, index * 16)
            }
        }
    }
}

class BooleanUniform(val baseUniform: Uniform<Float>) : Uniform<Boolean>(baseUniform.name, baseUniform.type, baseUniform.numComponents, baseUniform.count) {
    private val values = Array(numComponents * count) { false }

    override fun update(block: (Array<Boolean>) -> Unit) {
        block(values)

        baseUniform.update {
            values.forEachIndexed { index, value ->
                it[index] = if (value) 1.0f else 0.0f
            }
        }
    }
}

class TextureUniform<T : Texture>(val baseUniform: Uniform<T>) : Uniform<T>(baseUniform.name, baseUniform.type, baseUniform.numComponents, baseUniform.count) {
    override fun update(block: (Array<T>) -> Unit) {
        baseUniform.update(block)
    }
}

class TextureImage2D(val texture: Texture2D, val level: Int)

infix fun Texture2D.at(level: Int) = TextureImage2D(this, level)

class TextureImage3D(val texture: Texture3D, val level: Int)

infix fun Texture3D.at(level: Int) = TextureImage3D(this, level)

abstract class Image2DUniform(name: String) : Uniform<TextureImage2D>(name, Type.IMAGE_2D, 1, 1)

abstract class Image3DUniform(name: String) : Uniform<TextureImage3D>(name, Type.IMAGE_3D, 1, 1)

abstract class BufferUniform(name: String, val size: Int) : Uniform<GraphicsBuffer?>(name, Type.BUFFER, 1, 1) {
    fun createBuffer(usage: GraphicsBuffer.Usage) = Kore.graphics.createBuffer(usage) { setSize(size) }
}
