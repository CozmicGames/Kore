package com.cozmicgames.utils.maths

import com.cozmicgames.utils.extensions.sumOf
import kotlin.math.sqrt

abstract class FloatVector<V : FloatVector<V>>(size: Int) : Vector<Float, V>() {
    val lengthSquared get() = data.sumOf { it * it }

    val length get() = sqrt(lengthSquared)

    override val data = Array(size) { 0.0f }

    @Suppress("UNCHECKED_CAST")
    override fun set(vector: V): V {
        vector.data.copyInto(data)
        return this as V
    }

    fun normalize() = mul(1.0f / length)

    @Suppress("UNCHECKED_CAST")
    override fun set(scalar: Float): V {
        data.fill(scalar)
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun setZero(): V {
        data.fill(0.0f)
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun add(vector: V): V {
        repeat(size) {
            data[it] += vector.data[it]
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun sub(vector: V): V {
        repeat(size) {
            data[it] -= vector.data[it]
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun mul(vector: V): V {
        repeat(size) {
            data[it] *= vector.data[it]
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun div(vector: V): V {
        repeat(size) {
            data[it] /= vector.data[it]
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun add(scalar: Float): V {
        repeat(size) {
            data[it] += scalar
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun sub(scalar: Float): V {
        repeat(size) {
            data[it] -= scalar
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun mul(scalar: Float): V {
        repeat(size) {
            data[it] *= scalar
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun div(scalar: Float): V {
        repeat(size) {
            data[it] /= scalar
        }
        return this as V
    }

    @Suppress("UNCHECKED_CAST")
    override fun lerp(vector: V, alpha: Float): V {
        val oneMinusAplpha = 1.0f - alpha
        repeat(size) {
            data[it] = (data[it] * oneMinusAplpha) + (vector.data[it] * alpha)
        }
        return this as V
    }

    override fun negate() = mul(-1.0f)
}