package com.cozmicgames.utils.maths

abstract class IntVector<V : IntVector<V>>(size: Int) : Vector<Int, V>() {
    override val data = Array(size) { 0 }

    override fun set(vector: V): V {
        vector.data.copyInto(data)
        return this as V
    }

    override fun set(scalar: Int): V {
        data.fill(scalar)
        return this as V
    }

    override fun setZero(): V {
        data.fill(0)
        return this as V
    }

    override fun add(vector: V): V {
        repeat(size) {
            data[it] += vector.data[it]
        }
        return this as V
    }

    override fun sub(vector: V): V {
        repeat(size) {
            data[it] -= vector.data[it]
        }
        return this as V
    }

    override fun mul(vector: V): V {
        repeat(size) {
            data[it] *= vector.data[it]
        }
        return this as V
    }

    override fun div(vector: V): V {
        repeat(size) {
            data[it] /= vector.data[it]
        }
        return this as V
    }

    override fun add(scalar: Int): V {
        repeat(size) {
            data[it] += scalar
        }
        return this as V
    }

    override fun sub(scalar: Int): V {
        repeat(size) {
            data[it] -= scalar
        }
        return this as V
    }

    override fun mul(scalar: Int): V {
        repeat(size) {
            data[it] *= scalar
        }
        return this as V
    }

    override fun div(scalar: Int): V {
        repeat(size) {
            data[it] /= scalar
        }
        return this as V
    }

    override fun lerp(vector: V, alpha: Float): V {
        val oneMinusAplpha = 1.0f - alpha
        repeat(size) {
            data[it] = ((data[it] * oneMinusAplpha) + (vector.data[it] * alpha)).toInt()
        }
        return this as V
    }

    override fun negate() = mul(-1)
}